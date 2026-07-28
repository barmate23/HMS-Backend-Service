package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.GuestRequest;
import com.hotelerp.frontoffice.dto.ReservationRequest;
import com.hotelerp.frontoffice.dto.channex.ChannexBooking;
import com.hotelerp.frontoffice.dto.channex.ChannexCustomer;
import com.hotelerp.frontoffice.dto.channex.ChannexRoom;
import com.hotelerp.frontoffice.dto.channex.ChannexWebhookPayload;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannexWebhookServiceImpl implements ChannexWebhookService {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final RatePlanRepository ratePlanRepository;

    @Override
    @Transactional
    public StandardResponse<?> processBookingWebhook(ChannexWebhookPayload payload) {
        if (payload == null || payload.getPayload() == null || payload.getPayload().getBooking() == null) {
            log.warn("Received empty or invalid Channex webhook payload");
            return StandardResponse.error("Invalid webhook payload structure", "INVALID_PAYLOAD", null);
        }

        ChannexBooking bookingData = payload.getPayload().getBooking();
        String status = bookingData.getStatus() != null ? bookingData.getStatus().toLowerCase() : "new";
        String channexRef = bookingData.getOtaReservationCode() != null ? bookingData.getOtaReservationCode() : bookingData.getId();

        log.info("Processing Channex booking webhook: ref={}, status={}, ota={}", channexRef, status, bookingData.getOtaName());

        if ("cancelled".equals(status)) {
            return handleCancellation(channexRef);
        } else if ("modified".equals(status)) {
            return handleModification(bookingData, channexRef);
        } else {
            return handleNewBooking(bookingData, channexRef);
        }
    }

    private StandardResponse<?> handleNewBooking(ChannexBooking bookingData, String channexRef) {
        // 1. Idempotency Check: if booking reference already exists, ignore/return success
        Optional<Reservation> existingOpt = reservationRepository.findAll().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()) && channexRef.equalsIgnoreCase(r.getBookingReference()))
                .findFirst();

        if (existingOpt.isPresent()) {
            log.info("Booking reference {} already exists. Skipping creation.", channexRef);
            return StandardResponse.success("Booking reference already processed");
        }

        // 2. Build ReservationRequest DTO compatible with existing createReservation logic
        ReservationRequest req = new ReservationRequest();

        // Dates
        LocalDate checkIn = LocalDate.parse(bookingData.getArrivalDate());
        LocalDate checkOut = LocalDate.parse(bookingData.getDepartureDate());
        req.setCheckInDate(checkIn);
        req.setCheckOutDate(checkOut);

        // Guest handling
        ChannexCustomer customer = bookingData.getCustomer();
        if (customer != null) {
            String fullName = customer.getName() != null ? customer.getName().trim() : "OTA Guest";
            String[] names = fullName.split("\\s+", 2);
            String firstName = names[0];
            String lastName = names.length > 1 ? names[1] : "Guest";

            String email = customer.getMail() != null && !customer.getMail().isBlank() 
                    ? customer.getMail() 
                    : "ota_" + System.currentTimeMillis() + "@channex.booking";

            Optional<Guest> existingGuest = guestRepository.findByEmailAndIsDeletedFalse(email);
            if (existingGuest.isPresent()) {
                req.setGuestId(existingGuest.get().getId());
            } else {
                GuestRequest gd = new GuestRequest();
                gd.setFirstName(firstName);
                gd.setLastName(lastName);
                gd.setEmail(email);
                gd.setPhone(customer.getPhone() != null ? customer.getPhone() : "0000000000");
                gd.setAddressLine1(customer.getAddress());
                gd.setCity(customer.getCity());
                gd.setCountry(customer.getCountry());
                req.setGuestDetails(gd);
            }
        } else {
            GuestRequest gd = new GuestRequest();
            gd.setFirstName("OTA");
            gd.setLastName("Guest");
            gd.setEmail("ota_" + System.currentTimeMillis() + "@channex.booking");
            gd.setPhone("0000000000");
            req.setGuestDetails(gd);
        }

        // Hotel ID default
        req.setHotelId(1L);

        // Room Matching Strategy
        List<Long> assignedRoomIds = resolveRooms(bookingData, checkIn, checkOut);
        if (assignedRoomIds.isEmpty()) {
            log.error("Unable to match or allocate any rooms for Channex booking {}", channexRef);
            return StandardResponse.error("No available rooms match the requested room titles", "ROOM_MATCH_FAILED", null);
        }
        req.setRoomIds(assignedRoomIds);

        // Adults / Children count
        int totalAdults = 0;
        int totalChildren = 0;
        if (bookingData.getRooms() != null) {
            for (ChannexRoom r : bookingData.getRooms()) {
                totalAdults += (r.getAdultsCount() != null ? r.getAdultsCount() : 1);
                totalChildren += (r.getChildrenCount() != null ? r.getChildrenCount() : 0);
            }
        }
        req.setNumberOfAdults(totalAdults > 0 ? totalAdults : 1);
        req.setNumberOfChildren(totalChildren);

        // Rate Plan Resolution
        RatePlan ratePlan = ratePlanRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .findFirst()
                .orElse(null);
        if (ratePlan == null) {
            return StandardResponse.error("No active Rate Plan configured in master", "RATE_PLAN_MISSING", null);
        }
        req.setRatePlanId(ratePlan.getId());

        // Billing & Metadata
        req.setGstPercent(0);
        req.setBookingReference(channexRef);
        req.setTravelAgentName(bookingData.getOtaName() != null ? bookingData.getOtaName() : "Channex");
        req.setBusinessSource("OTA - Channex");
        req.setMarketSegment("OTA");
        req.setNotes(bookingData.getNotes());

        return reservationService.createReservation(req);
    }

    private StandardResponse<?> handleModification(ChannexBooking bookingData, String channexRef) {
        Optional<Reservation> existingOpt = reservationRepository.findAll().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()) && channexRef.equalsIgnoreCase(r.getBookingReference()))
                .findFirst();

        if (existingOpt.isEmpty()) {
            log.warn("Modification received for unknown reference {}. Creating as new booking.", channexRef);
            return handleNewBooking(bookingData, channexRef);
        }

        Reservation existing = existingOpt.get();

        ReservationRequest req = new ReservationRequest();
        LocalDate checkIn = LocalDate.parse(bookingData.getArrivalDate());
        LocalDate checkOut = LocalDate.parse(bookingData.getDepartureDate());
        req.setCheckInDate(checkIn);
        req.setCheckOutDate(checkOut);
        req.setHotelId(1L);
        req.setGuestId(existing.getGuest() != null ? existing.getGuest().getId() : null);

        List<Long> assignedRoomIds = resolveRooms(bookingData, checkIn, checkOut);
        if (!assignedRoomIds.isEmpty()) {
            req.setRoomIds(assignedRoomIds);
        }

        int totalAdults = 0;
        int totalChildren = 0;
        if (bookingData.getRooms() != null) {
            for (ChannexRoom r : bookingData.getRooms()) {
                totalAdults += (r.getAdultsCount() != null ? r.getAdultsCount() : 1);
                totalChildren += (r.getChildrenCount() != null ? r.getChildrenCount() : 0);
            }
        }
        req.setNumberOfAdults(totalAdults > 0 ? totalAdults : 1);
        req.setNumberOfChildren(totalChildren);
        req.setRatePlanId(existing.getRatePlan() != null ? existing.getRatePlan().getId() : 1L);
        req.setBookingReference(channexRef);
        req.setNotes(bookingData.getNotes());

        return reservationService.updateReservation(existing.getId(), req);
    }

    private StandardResponse<?> handleCancellation(String channexRef) {
        Optional<Reservation> existingOpt = reservationRepository.findAll().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()) && channexRef.equalsIgnoreCase(r.getBookingReference()))
                .findFirst();

        if (existingOpt.isPresent()) {
            return reservationService.cancelReservation(existingOpt.get().getId());
        }

        log.warn("Cancellation received for non-existent reservation ref: {}", channexRef);
        return StandardResponse.success("Reservation not found for cancellation, ignored");
    }

    private List<Long> resolveRooms(ChannexBooking bookingData, LocalDate checkIn, LocalDate checkOut) {
        List<Long> assignedRoomIds = new ArrayList<>();
        List<Room> availableRooms = roomRepository.findAvailableRooms(checkIn, checkOut);

        if (bookingData.getRooms() != null && !bookingData.getRooms().isEmpty()) {
            for (ChannexRoom cRoom : bookingData.getRooms()) {
                String title = cRoom.getTitle();
                Optional<RoomType> matchedType = Optional.empty();
                if (title != null && !title.isBlank()) {
                    matchedType = roomTypeRepository.findByNameIgnoreCaseAndIsActiveTrue(title.trim());
                }

                Room selectedRoom = null;
                if (matchedType.isPresent()) {
                    Long typeId = matchedType.get().getId();
                    selectedRoom = availableRooms.stream()
                            .filter(r -> r.getRoomType() != null && r.getRoomType().getId().equals(typeId))
                            .filter(r -> !assignedRoomIds.contains(r.getId()))
                            .findFirst()
                            .orElse(null);
                }

                // Fallback: pick any available room if room type didn't match or was fully booked
                if (selectedRoom == null) {
                    selectedRoom = availableRooms.stream()
                            .filter(r -> !assignedRoomIds.contains(r.getId()))
                            .findFirst()
                            .orElse(null);
                }

                if (selectedRoom != null) {
                    assignedRoomIds.add(selectedRoom.getId());
                }
            }
        } else {
            // No explicit room line, grab first available room
            if (!availableRooms.isEmpty()) {
                assignedRoomIds.add(availableRooms.get(0).getId());
            }
        }

        return assignedRoomIds;
    }
}
