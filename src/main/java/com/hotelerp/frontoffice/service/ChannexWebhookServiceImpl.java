package com.hotelerp.frontoffice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.GuestRequest;
import com.hotelerp.frontoffice.dto.ReservationRequest;
import com.hotelerp.frontoffice.entity.Guest;
import com.hotelerp.frontoffice.entity.RatePlan;
import com.hotelerp.frontoffice.entity.Reservation;
import com.hotelerp.frontoffice.entity.Room;
import com.hotelerp.frontoffice.entity.RoomType;
import com.hotelerp.frontoffice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enterprise Channex Webhook & Sync Service.
 * Robustly parses flat, nested, or JSON:API Channex payloads with exhaustive fallback chains.
 * Correctly aligns pricing, room matching, and GST tax configuration with HMS core logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChannexWebhookServiceImpl implements ChannexWebhookService {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;

    @Override
    @Transactional
    public StandardResponse<?> processBookingWebhook(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return StandardResponse.error("Received null or empty payload", "INVALID_PAYLOAD", null);
        }

        // ══════════════════════════════════════════════════════════════════════
        // 1. Exhaustive Fallback Resolution for Check-In / Arrival Date
        // ══════════════════════════════════════════════════════════════════════
        String arrivalDate = resolveArrivalDate(root);

        if (arrivalDate == null || arrivalDate.isBlank()) {
            log.error("Missing arrival_date across all payload locations. Raw: {}", root);
            return StandardResponse.error("Missing arrival_date in payload", "INVALID_PAYLOAD", null);
        }

        // ══════════════════════════════════════════════════════════════════════
        // 2. Exhaustive Fallback Resolution for Booking Reference
        // ══════════════════════════════════════════════════════════════════════
        String bookingRef = resolveBookingReference(root);
        if (bookingRef == null || bookingRef.isBlank()) {
            bookingRef = "OTA-" + System.currentTimeMillis();
            log.warn("Unable to resolve booking reference, using fallback: {}", bookingRef);
        }

        // ══════════════════════════════════════════════════════════════════════
        // 3. Resolve Booking Status
        // ══════════════════════════════════════════════════════════════════════
        String status = resolveString(root, "status");
        if (status == null) status = "new";
        status = status.toLowerCase();

        log.info("Channex Webhook Received: ref={}, arrival={}, status={}", bookingRef, arrivalDate, status);

        if ("cancelled".equals(status) || "canceled".equals(status)) {
            return handleCancellation(bookingRef);
        }

        // ══════════════════════════════════════════════════════════════════════
        // 4. Idempotency Check — Skip duplicate processing
        // ══════════════════════════════════════════════════════════════════════
        final String finalRef = bookingRef;
        Optional<Reservation> existingOpt = reservationRepository.findAll().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()) && finalRef.equalsIgnoreCase(r.getBookingReference()))
                .findFirst();

        if (existingOpt.isPresent()) {
            log.info("Booking reference {} already exists (Reservation #{}). Skipping creation.", bookingRef, existingOpt.get().getId());
            return StandardResponse.success("Booking reference already processed");
        }

        // ══════════════════════════════════════════════════════════════════════
        // 5. Calculate Check-Out Date & Stay Duration
        // ══════════════════════════════════════════════════════════════════════
        LocalDate checkIn = LocalDate.parse(arrivalDate.trim());
        String departureDate = resolveDepartureDate(root);
        int countOfNights = resolveInt(root, "count_of_nights", "nights");

        LocalDate checkOut;
        if (departureDate != null && !departureDate.isBlank()) {
            checkOut = LocalDate.parse(departureDate.trim());
        } else {
            checkOut = checkIn.plusDays(countOfNights > 0 ? countOfNights : 1);
        }

        if (!checkOut.isAfter(checkIn)) {
            checkOut = checkIn.plusDays(1);
        }

        // ══════════════════════════════════════════════════════════════════════
        // 6. Guest Details Extraction & Matching
        // ══════════════════════════════════════════════════════════════════════
        String fullName = resolveCustomerName(root);
        String firstName;
        String lastName;
        if (fullName != null && !fullName.isBlank() && !"OTA Guest".equalsIgnoreCase(fullName)) {
            String[] nameParts = fullName.trim().split("\\s+", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        } else {
            String fn = resolveString(root, "first_name", "customer_first_name");
            String ln = resolveString(root, "last_name", "surname", "customer_last_name");
            firstName = (fn != null && !fn.isBlank()) ? fn.trim() : "OTA";
            lastName = (ln != null && !ln.isBlank()) ? ln.trim() : "Guest";
        }

        String customerEmail = resolveCustomerEmail(root, finalRef);
        String customerPhone = resolveCustomerPhone(root);
        String addressLine1 = resolveString(root, "customer_address", "address", "address_line1", "street");
        String city = resolveString(root, "customer_city", "city");
        String state = resolveString(root, "customer_state", "state", "province");
        String postCode = resolveString(root, "customer_zip", "customer_postcode", "zip", "post_code", "postal_code");
        String country = resolveString(root, "customer_country", "country", "country_code");

        ReservationRequest req = new ReservationRequest();
        req.setCheckInDate(checkIn);
        req.setCheckOutDate(checkOut);
        req.setHotelId(1L);

        GuestRequest gd = new GuestRequest();
        gd.setFirstName(firstName);
        gd.setLastName(lastName);
        gd.setEmail(customerEmail);
        gd.setPhone(customerPhone != null ? customerPhone : "0000000000");
        gd.setAddressLine1(addressLine1);
        gd.setCity(city);
        gd.setState(state);
        gd.setPostCode(postCode);
        gd.setCountry(country);

        Optional<Guest> existingGuestOpt = guestRepository.findByEmailAndIsDeletedFalse(customerEmail);
        if (existingGuestOpt.isPresent()) {
            Guest existingGuest = existingGuestOpt.get();
            existingGuest.setFirstName(firstName);
            existingGuest.setLastName(lastName);
            if (customerPhone != null && !"0000000000".equals(customerPhone)) {
                existingGuest.setPhone(customerPhone);
            }
            if (addressLine1 != null && !addressLine1.isBlank()) existingGuest.setAddressLine1(addressLine1);
            if (city != null && !city.isBlank()) existingGuest.setCity(city);
            if (state != null && !state.isBlank()) existingGuest.setState(state);
            if (postCode != null && !postCode.isBlank()) existingGuest.setPostCode(postCode);
            if (country != null && !country.isBlank()) existingGuest.setCountry(country);
            existingGuest.setUpdatedAt(LocalDateTime.now());
            existingGuest = guestRepository.save(existingGuest);
            req.setGuestId(existingGuest.getId());
        } else {
            req.setGuestDetails(gd);
        }

        // ══════════════════════════════════════════════════════════════════════
        // 7. Room Allocation & Room Matching
        // ══════════════════════════════════════════════════════════════════════
        int countOfRooms = resolveInt(root, "count_of_rooms", "rooms");
        if (countOfRooms <= 0) countOfRooms = 1;

        List<Long> assignedRoomIds = allocateRooms(root, checkIn, checkOut, countOfRooms);
        if (assignedRoomIds.isEmpty()) {
            log.error("Unable to allocate rooms for Channex booking {} (CheckIn={}, CheckOut={})", bookingRef, checkIn, checkOut);
            return StandardResponse.error("No available rooms for requested dates", "ROOM_MATCH_FAILED", null);
        }
        req.setRoomIds(assignedRoomIds);

        // ══════════════════════════════════════════════════════════════════════
        // 8. Occupancy, Rate Plan & GST Tax Resolution
        // ══════════════════════════════════════════════════════════════════════
        int adults = resolveInt(root, "occupancy.adults", "adults_count", "adults");
        int children = resolveInt(root, "occupancy.children", "children_count", "children");
        req.setNumberOfAdults(adults > 0 ? adults : 1);
        req.setNumberOfChildren(children >= 0 ? children : 0);

        RatePlan ratePlan = ratePlanRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .findFirst().orElse(null);
        if (ratePlan == null) {
            return StandardResponse.error("No active Rate Plan configured in master", "RATE_PLAN_MISSING", null);
        }
        req.setRatePlanId(ratePlan.getId());

        // GST Tax Rate: Default to 18% for HMS view screen alignment, or parse if provided
        int gstPercent = resolveInt(root, "gst_percent", "tax_percent", "gst_rate");
        if (gstPercent <= 0) {
            gstPercent = 18; // Standard HMS FrontOffice GST rate
        }
        req.setGstPercent(gstPercent);

        // Metadata
        String resolvedBookingFrom = resolveString(root, "booking_from", "bookingFrom", "ota_name", "channel_name", "channel", "source", "system_source", "travel_agent_name", "ota");
        if (resolvedBookingFrom == null || resolvedBookingFrom.isBlank()) {
            resolvedBookingFrom = "Channex";
        }
        String notes = resolveString(root, "notes", "special_requests");
        req.setBookingReference(bookingRef);
        req.setTravelAgentName(resolvedBookingFrom);
        req.setBookingFrom(resolvedBookingFrom);
        req.setBusinessSource("OTA - " + resolvedBookingFrom);
        req.setMarketSegment("OTA");
        req.setNotes(notes);

        log.info("Submitting reservation request: ref={}, guest={}, checkIn={}, checkOut={}, rooms={}, gst={}%",
                bookingRef, fullName, checkIn, checkOut, assignedRoomIds, gstPercent);

        return reservationService.createReservation(req);
    }

    private StandardResponse<?> handleCancellation(String bookingRef) {
        Optional<Reservation> existingOpt = reservationRepository.findAll().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()) && bookingRef.equalsIgnoreCase(r.getBookingReference()))
                .findFirst();

        if (existingOpt.isPresent()) {
            return reservationService.cancelReservation(existingOpt.get().getId());
        }
        log.warn("Cancellation received for non-existent reservation ref: {}", bookingRef);
        return StandardResponse.success("Reservation not found for cancellation, ignored");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helper Fallback Resolvers
    // ══════════════════════════════════════════════════════════════════════════

    private String resolveArrivalDate(JsonNode root) {
        String val = findStringInNode(root, "arrival_date", "checkin_date", "check_in_date");
        if (val != null) return val;

        val = findStringInNode(root.path("payload"), "arrival_date", "checkin_date");
        if (val != null) return val;

        val = findStringInNode(root.path("booking"), "arrival_date", "checkin_date");
        if (val != null) return val;

        val = findStringInNode(root.path("payload").path("booking"), "arrival_date", "checkin_date");
        if (val != null) return val;

        val = findStringInNode(root.path("attributes"), "arrival_date", "checkin_date");
        if (val != null) return val;

        val = findStringInNode(root.path("data").path("attributes"), "arrival_date", "checkin_date");
        if (val != null) return val;

        if (root.path("rooms").isArray() && root.path("rooms").size() > 0) {
            val = findStringInNode(root.path("rooms").get(0), "checkin_date", "arrival_date");
            if (val != null) return val;
        }

        return null;
    }

    private String resolveDepartureDate(JsonNode root) {
        String val = findStringInNode(root, "departure_date", "checkout_date", "check_out_date");
        if (val != null) return val;

        val = findStringInNode(root.path("payload"), "departure_date", "checkout_date");
        if (val != null) return val;

        val = findStringInNode(root.path("booking"), "departure_date", "checkout_date");
        if (val != null) return val;

        val = findStringInNode(root.path("payload").path("booking"), "departure_date", "checkout_date");
        if (val != null) return val;

        val = findStringInNode(root.path("attributes"), "departure_date", "checkout_date");
        if (val != null) return val;

        if (root.path("rooms").isArray() && root.path("rooms").size() > 0) {
            val = findStringInNode(root.path("rooms").get(0), "checkout_date", "departure_date");
            if (val != null) return val;
        }

        return null;
    }

    private String resolveBookingReference(JsonNode root) {
        String val = findStringInNode(root, "booking_unique_id", "unique_id", "ota_code", "ota_reservation_code", "booking_id", "id");
        if (val != null) return val;

        val = findStringInNode(root.path("payload"), "booking_unique_id", "unique_id", "ota_reservation_code", "booking_id");
        if (val != null) return val;

        val = findStringInNode(root.path("booking"), "booking_unique_id", "unique_id", "ota_reservation_code", "booking_id");
        if (val != null) return val;

        val = findStringInNode(root.path("payload").path("booking"), "booking_unique_id", "unique_id", "ota_reservation_code", "id");
        if (val != null) return val;

        val = findStringInNode(root.path("attributes"), "booking_unique_id", "ota_reservation_code", "booking_id");
        if (val != null) return val;

        return null;
    }

    private String resolveCustomerName(JsonNode root) {
        String name = resolveString(root, "customer_name", "guest_name", "billing_name", "name");
        if (name != null && !name.isBlank()) return name.trim();

        String firstName = resolveString(root, "first_name", "customer_first_name");
        String lastName = resolveString(root, "last_name", "surname", "customer_last_name");
        if (firstName != null && lastName != null) return firstName.trim() + " " + lastName.trim();
        if (firstName != null) return firstName.trim();
        if (lastName != null) return lastName.trim();

        return "OTA Guest";
    }

    private String resolveCustomerEmail(JsonNode root, String bookingRef) {
        String email = resolveString(root, "customer_email", "email", "mail");
        if (email != null && !email.isBlank()) return email.trim();

        return "ota_" + bookingRef.replaceAll("[^a-zA-Z0-9]", "") + "@channex.booking";
    }

    private String resolveCustomerPhone(JsonNode root) {
        String phone = resolveString(root, "customer_phone", "phone", "mobile", "telephone");
        if (phone != null && !phone.isBlank()) return phone.trim();

        return "0000000000";
    }

    private String resolveString(JsonNode root, String... fields) {
        String val = findStringInNode(root, fields);
        if (val != null) return val;
        val = findStringInNode(root.path("payload"), fields);
        if (val != null) return val;
        val = findStringInNode(root.path("booking"), fields);
        if (val != null) return val;
        val = findStringInNode(root.path("payload").path("booking"), fields);
        if (val != null) return val;
        val = findStringInNode(root.path("attributes"), fields);
        if (val != null) return val;
        val = findStringInNode(root.path("customer"), fields);
        if (val != null) return val;
        val = findStringInNode(root.path("payload").path("booking").path("customer"), fields);
        if (val != null) return val;
        return findStringInNode(root.path("attributes").path("customer"), fields);
    }

    private int resolveInt(JsonNode root, String... fields) {
        for (String f : fields) {
            JsonNode n = resolveNodeByPath(root, f);
            if (n != null && n.isValueNode()) return n.asInt(0);
        }
        return 0;
    }

    private JsonNode resolveNodeByPath(JsonNode root, String path) {
        if (root == null || root.isMissingNode()) return null;
        if (path.contains(".")) {
            String[] parts = path.split("\\.");
            JsonNode current = root;
            for (String p : parts) {
                current = current.path(p);
                if (current.isMissingNode() || current.isNull()) break;
            }
            if (current != null && !current.isMissingNode() && !current.isNull()) return current;
        } else {
            JsonNode child = root.path(path);
            if (!child.isMissingNode() && !child.isNull()) return child;
        }
        return null;
    }

    private String findStringInNode(JsonNode node, String... keys) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        for (String k : keys) {
            JsonNode child = node.path(k);
            if (!child.isMissingNode() && !child.isNull() && child.isValueNode()) {
                String text = child.asText();
                if (text != null && !text.isBlank()) return text;
            }
        }
        return null;
    }

    private List<Long> allocateRooms(JsonNode root, LocalDate checkIn, LocalDate checkOut, int countOfRooms) {
        List<Room> availableRooms = roomRepository.findAvailableRooms(checkIn, checkOut);
        List<Long> assigned = new ArrayList<>();

        // 1. Try matching by room_type_name or title if rooms array exists in payload
        JsonNode roomsArray = root.path("rooms");
        if (roomsArray.isMissingNode()) roomsArray = root.path("payload").path("booking").path("rooms");

        if (roomsArray.isArray() && roomsArray.size() > 0) {
            for (JsonNode rNode : roomsArray) {
                String title = findStringInNode(rNode, "title", "room_type", "room_type_name", "name");
                Room matchedRoom = null;
                if (title != null && !title.isBlank()) {
                    Optional<RoomType> matchedType = roomTypeRepository.findByNameIgnoreCaseAndIsActiveTrue(title.trim());
                    if (matchedType.isPresent()) {
                        Long typeId = matchedType.get().getId();
                        matchedRoom = availableRooms.stream()
                                .filter(r -> r.getRoomType() != null && r.getRoomType().getId().equals(typeId))
                                .filter(r -> !assigned.contains(r.getId()))
                                .findFirst().orElse(null);
                    }
                }
                if (matchedRoom == null) {
                    matchedRoom = availableRooms.stream()
                            .filter(r -> !assigned.contains(r.getId()))
                            .findFirst().orElse(null);
                }
                if (matchedRoom != null) {
                    assigned.add(matchedRoom.getId());
                }
            }
        }

        // 2. Fallback: allocate available rooms up to countOfRooms
        while (assigned.size() < countOfRooms) {
            Room fallback = availableRooms.stream()
                    .filter(r -> !assigned.contains(r.getId()))
                    .findFirst().orElse(null);
            if (fallback != null) {
                assigned.add(fallback.getId());
            } else {
                break;
            }
        }

        return assigned;
    }
}
