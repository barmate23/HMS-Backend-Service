package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.GuestRequest;
import com.hotelerp.frontoffice.dto.ReservationRequest;
import com.hotelerp.frontoffice.dto.channex.ChannexWebhookPayload;
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
    public StandardResponse<?> processBookingWebhook(ChannexWebhookPayload payload) {
        // ══════════════════════════════════════════════════════════════════════
        // Validate required fields from the flat Channex payload
        // ══════════════════════════════════════════════════════════════════════
        if (payload == null || payload.getBookingUniqueId() == null) {
            log.warn("Received empty or invalid Channex webhook payload (no booking_unique_id)");
            return StandardResponse.error("Invalid webhook payload - missing booking_unique_id", "INVALID_PAYLOAD", null);
        }

        String bookingRef = payload.getBookingUniqueId(); // e.g. "GBB-123456"
        String status = payload.getStatus() != null ? payload.getStatus().toLowerCase() : "new";

        log.info("Processing Channex booking: ref={}, customer={}, arrival={}, nights={}, rooms={}, status={}",
                bookingRef, payload.getCustomerName(), payload.getArrivalDate(),
                payload.getCountOfNights(), payload.getCountOfRooms(), status);

        // Handle cancellation
        if ("cancelled".equals(status)) {
            return handleCancellation(bookingRef);
        }

        // ══════════════════════════════════════════════════════════════════════
        // Idempotency: skip if booking reference already exists
        // ══════════════════════════════════════════════════════════════════════
        Optional<Reservation> existingOpt = reservationRepository.findAll().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()) && bookingRef.equalsIgnoreCase(r.getBookingReference()))
                .findFirst();

        if (existingOpt.isPresent()) {
            log.info("Booking reference {} already exists (reservation #{}). Skipping.", bookingRef, existingOpt.get().getId());
            return StandardResponse.success("Booking reference already processed");
        }

        // ══════════════════════════════════════════════════════════════════════
        // Build ReservationRequest from flat payload fields
        // ══════════════════════════════════════════════════════════════════════
        ReservationRequest req = new ReservationRequest();

        // Dates — compute checkout from arrival + nights
        LocalDate checkIn = LocalDate.parse(payload.getArrivalDate());
        int nights = payload.getCountOfNights() != null && payload.getCountOfNights() > 0
                ? payload.getCountOfNights() : 1;
        LocalDate checkOut;
        if (payload.getDepartureDate() != null && !payload.getDepartureDate().isBlank()) {
            checkOut = LocalDate.parse(payload.getDepartureDate());
        } else {
            checkOut = checkIn.plusDays(nights);
        }
        req.setCheckInDate(checkIn);
        req.setCheckOutDate(checkOut);

        // Guest — "customer_name" is a flat string like "Genz youth youth"
        String customerName = payload.getCustomerName() != null ? payload.getCustomerName().trim() : "OTA Guest";
        String[] nameParts = customerName.split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "Guest";

        String email = (payload.getCustomerEmail() != null && !payload.getCustomerEmail().isBlank())
                ? payload.getCustomerEmail()
                : "ota_" + bookingRef.replaceAll("[^a-zA-Z0-9]", "") + "@channex.booking";

        Optional<Guest> existingGuest = guestRepository.findByEmailAndIsDeletedFalse(email);
        if (existingGuest.isPresent()) {
            req.setGuestId(existingGuest.get().getId());
        } else {
            GuestRequest gd = new GuestRequest();
            gd.setFirstName(firstName);
            gd.setLastName(lastName);
            gd.setEmail(email);
            gd.setPhone(payload.getCustomerPhone() != null ? payload.getCustomerPhone() : "0000000000");
            req.setGuestDetails(gd);
        }

        // Hotel
        req.setHotelId(1L);

        // Room Allocation — pick available rooms (Channex doesn't send room type name)
        int roomCount = payload.getCountOfRooms() != null && payload.getCountOfRooms() > 0
                ? payload.getCountOfRooms() : 1;
        List<Room> availableRooms = roomRepository.findAvailableRooms(checkIn, checkOut);
        List<Long> assignedRoomIds = new ArrayList<>();
        for (int i = 0; i < roomCount && i < availableRooms.size(); i++) {
            assignedRoomIds.add(availableRooms.get(i).getId());
        }
        if (assignedRoomIds.isEmpty()) {
            log.error("No available rooms for Channex booking {} (checkIn={}, checkOut={})", bookingRef, checkIn, checkOut);
            return StandardResponse.error("No available rooms for requested dates", "ROOM_MATCH_FAILED", null);
        }
        req.setRoomIds(assignedRoomIds);

        // Adults / Children
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
        req.setTravelAgentName(payload.getOtaName() != null ? payload.getOtaName() : "Channex");
        req.setBusinessSource("OTA - Channex");
        req.setMarketSegment("OTA");
        req.setNotes(payload.getNotes());

        log.info("Creating reservation from Channex: ref={}, guest={} {}, checkIn={}, checkOut={}, rooms={}",
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
}
