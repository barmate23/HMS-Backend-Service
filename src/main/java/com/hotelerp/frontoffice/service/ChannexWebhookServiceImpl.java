package com.hotelerp.frontoffice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.GuestRequest;
import com.hotelerp.frontoffice.dto.ReservationRequest;
import com.hotelerp.frontoffice.entity.Guest;
import com.hotelerp.frontoffice.entity.RatePlan;
import com.hotelerp.frontoffice.entity.Reservation;
import com.hotelerp.frontoffice.entity.Room;
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
 * Processes Channex webhook payloads using JsonNode for maximum resilience.
 * 
 * Real Channex flat payload fields:
 *   booking_unique_id, customer_name, arrival_date, count_of_nights,
 *   count_of_rooms, amount, currency, ota_code, ota_name, status, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChannexWebhookServiceImpl implements ChannexWebhookService {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final RatePlanRepository ratePlanRepository;

    @Override
    @Transactional
    public StandardResponse<?> processBookingWebhook(JsonNode root) {
        // ══════════════════════════════════════════════════════════════════════
        // Read fields directly from JsonNode — no DTO mapping issues possible
        // ══════════════════════════════════════════════════════════════════════
        String bookingRef = getTextOrNull(root, "booking_unique_id");
        String customerName = getTextOrNull(root, "customer_name");
        String arrivalDate = getTextOrNull(root, "arrival_date");
        String departureDate = getTextOrNull(root, "departure_date");
        int countOfNights = root.path("count_of_nights").asInt(1);
        int countOfRooms = root.path("count_of_rooms").asInt(1);
        String status = root.path("status").asText("new").toLowerCase();
        String otaName = getTextOrNull(root, "ota_name");
        String otaCode = getTextOrNull(root, "ota_code");
        String customerEmail = getTextOrNull(root, "customer_email");
        String customerPhone = getTextOrNull(root, "customer_phone");
        String notes = getTextOrNull(root, "notes");
        String bookingId = getTextOrNull(root, "booking_id");

        log.info("Channex fields: bookingRef={}, customer={}, arrival={}, nights={}, rooms={}, status={}, otaCode={}",
                bookingRef, customerName, arrivalDate, countOfNights, countOfRooms, status, otaCode);

        // Fallback booking reference: booking_unique_id → booking_id → ota_code
        if (bookingRef == null || bookingRef.isBlank()) {
            bookingRef = bookingId != null ? bookingId : ("OTA-" + (otaCode != null ? otaCode : System.currentTimeMillis()));
            log.warn("No booking_unique_id found, using fallback ref: {}", bookingRef);
        }

        if (arrivalDate == null || arrivalDate.isBlank()) {
            log.error("Missing arrival_date in Channex payload");
            return StandardResponse.error("Missing arrival_date in payload", "INVALID_PAYLOAD", null);
        }

        // Handle cancellation
        if ("cancelled".equals(status)) {
            return handleCancellation(bookingRef);
        }

        // ══════════════════════════════════════════════════════════════════════
        // Idempotency: skip if booking reference already exists
        // ══════════════════════════════════════════════════════════════════════
        final String finalRef = bookingRef;
        Optional<Reservation> existingOpt = reservationRepository.findAll().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()) && finalRef.equalsIgnoreCase(r.getBookingReference()))
                .findFirst();

        if (existingOpt.isPresent()) {
            log.info("Booking {} already exists (reservation #{}). Skipping.", bookingRef, existingOpt.get().getId());
            return StandardResponse.success("Booking reference already processed");
        }

        // ══════════════════════════════════════════════════════════════════════
        // Build ReservationRequest
        // ══════════════════════════════════════════════════════════════════════
        ReservationRequest req = new ReservationRequest();

        // Dates
        LocalDate checkIn = LocalDate.parse(arrivalDate);
        LocalDate checkOut;
        if (departureDate != null && !departureDate.isBlank()) {
            checkOut = LocalDate.parse(departureDate);
        } else {
            checkOut = checkIn.plusDays(countOfNights > 0 ? countOfNights : 1);
        }
        req.setCheckInDate(checkIn);
        req.setCheckOutDate(checkOut);

        // Guest — customer_name is a flat string like "Chanex Booking booking"
        String safeName = (customerName != null && !customerName.isBlank()) ? customerName.trim() : "OTA Guest";
        String[] nameParts = safeName.split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "Guest";

        String email = (customerEmail != null && !customerEmail.isBlank())
                ? customerEmail
                : "ota_" + finalRef.replaceAll("[^a-zA-Z0-9]", "") + "@channex.booking";

        Optional<Guest> existingGuest = guestRepository.findByEmailAndIsDeletedFalse(email);
        if (existingGuest.isPresent()) {
            req.setGuestId(existingGuest.get().getId());
        } else {
            GuestRequest gd = new GuestRequest();
            gd.setFirstName(firstName);
            gd.setLastName(lastName);
            gd.setEmail(email);
            gd.setPhone(customerPhone != null ? customerPhone : "0000000000");
            req.setGuestDetails(gd);
        }

        // Hotel
        req.setHotelId(1L);

        // Room Allocation
        List<Room> availableRooms = roomRepository.findAvailableRooms(checkIn, checkOut);
        List<Long> assignedRoomIds = new ArrayList<>();
        for (int i = 0; i < countOfRooms && i < availableRooms.size(); i++) {
            assignedRoomIds.add(availableRooms.get(i).getId());
        }
        if (assignedRoomIds.isEmpty()) {
            log.error("No available rooms for booking {} (checkIn={}, checkOut={})", bookingRef, checkIn, checkOut);
            return StandardResponse.error("No available rooms for requested dates", "ROOM_MATCH_FAILED", null);
        }
        req.setRoomIds(assignedRoomIds);

        // Occupancy
        req.setNumberOfAdults(1);
        req.setNumberOfChildren(0);

        // Rate Plan
        RatePlan ratePlan = ratePlanRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .findFirst().orElse(null);
        if (ratePlan == null) {
            return StandardResponse.error("No active Rate Plan configured", "RATE_PLAN_MISSING", null);
        }
        req.setRatePlanId(ratePlan.getId());

        // Billing & Metadata
        req.setGstPercent(0);
        req.setBookingReference(bookingRef);
        req.setTravelAgentName(otaName != null ? otaName : "Channex");
        req.setBusinessSource("OTA - Channex");
        req.setMarketSegment("OTA");
        req.setNotes(notes);

        log.info("Creating reservation: ref={}, guest={} {}, checkIn={}, checkOut={}, rooms={}",
                bookingRef, firstName, lastName, checkIn, checkOut, assignedRoomIds);

        return reservationService.createReservation(req);
    }

    private StandardResponse<?> handleCancellation(String bookingRef) {
        Optional<Reservation> existingOpt = reservationRepository.findAll().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()) && bookingRef.equalsIgnoreCase(r.getBookingReference()))
                .findFirst();

        if (existingOpt.isPresent()) {
            return reservationService.cancelReservation(existingOpt.get().getId());
        }
        log.warn("Cancellation for non-existent reservation ref: {}", bookingRef);
        return StandardResponse.success("Reservation not found for cancellation, ignored");
    }

    /** Safely read a text field from JsonNode, returning null if missing/empty */
    private String getTextOrNull(JsonNode node, String field) {
        JsonNode child = node.path(field);
        if (child.isMissingNode() || child.isNull()) return null;
        String val = child.asText();
        return (val != null && !val.isBlank()) ? val : null;
    }
}
