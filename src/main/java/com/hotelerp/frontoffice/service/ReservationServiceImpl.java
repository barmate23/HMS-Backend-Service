package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.*;
import com.hotelerp.frontoffice.entity.*;
import com.hotelerp.frontoffice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookingRepository     bookingRepository;
    private final GuestRepository       guestRepository;
    private final RoomRepository        roomRepository;
    private final BillRepository        billRepository;
    private final PaymentRepository     paymentRepository;
    private final RoomAuditRepository   roomAuditRepository;

    // Rate-plan add-ons per night (INR) — adjust or externalise as needed
    private static final Map<Reservation.RatePlan, BigDecimal> RATE_PLAN_CHARGES = Map.of(
            Reservation.RatePlan.EP,  BigDecimal.ZERO,
            Reservation.RatePlan.CP,  new BigDecimal("500"),
            Reservation.RatePlan.MAP, new BigDecimal("900"),
            Reservation.RatePlan.AP,  new BigDecimal("1400")
    );

    // ── Create ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<?> createReservation(ReservationRequest req) {
        log.info("Creating reservation for guestId={}, rooms={}", req.getGuestId(), req.getRoomIds());
        try {
            // 1. Resolve guest — either by existing ID or create a new one inline
            Guest guest;
            if (req.getGuestId() != null) {
                // ── Search Guest flow ─────────────────────────────────────
                guest = guestRepository.findById(req.getGuestId())
                        .filter(g -> !Boolean.TRUE.equals(g.getIsDeleted()))
                        .orElse(null);
                if (guest == null) {
                    return StandardResponse.error("Guest not found", "GUEST_NOT_FOUND", "guestId", null);
                }
                log.info("Using existing guestId={}", guest.getId());

            } else if (req.getGuestDetails() != null) {
                // ── Create Guest flow ─────────────────────────────────────
                GuestRequest gd = req.getGuestDetails();
                if (gd.getEmail() == null || gd.getEmail().isBlank()) {
                    return StandardResponse.error("Guest email is required", "GUEST_EMAIL_REQUIRED",
                            "guestDetails.email", null);
                }
                if (guestRepository.existsByEmailAndIsDeletedFalse(gd.getEmail())) {
                    // Guest already exists — use the existing record instead of failing
                    guest = guestRepository.searchGuests(gd.getEmail())
                            .stream().findFirst()
                            .orElse(null);
                    if (guest == null) {
                        return StandardResponse.error("Duplicate email and guest lookup failed",
                                "DUPLICATE_EMAIL", "guestDetails.email", null);
                    }
                    log.info("Inline guest email already exists; re-using guestId={}", guest.getId());
                } else {
                    guest = buildInlineGuest(gd);
                    guest = guestRepository.save(guest);
                    log.info("Inline guest created with ID={}", guest.getId());
                }

            } else {
                return StandardResponse.error(
                        "Either guestId or guestDetails must be provided",
                        "GUEST_REQUIRED", "guestId/guestDetails", null);
            }

            // 2. Validate rooms list
            if (req.getRoomIds() == null || req.getRoomIds().isEmpty()) {
                return StandardResponse.error("At least one room must be selected",
                        "NO_ROOMS_SELECTED", "roomIds", null);
            }

            // 3. Validate dates
            if (!req.getCheckOutDate().isAfter(req.getCheckInDate())) {
                return StandardResponse.error("Check-out date must be after check-in date",
                        "INVALID_DATES", "checkOutDate", null);
            }

            long nights = ChronoUnit.DAYS.between(req.getCheckInDate(), req.getCheckOutDate());

            // 4. Resolve & validate all rooms, check availability
            List<Room> rooms = new ArrayList<>();
            for (Long roomId : req.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                        .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                        .orElse(null);
                if (room == null) {
                    return StandardResponse.error("Room " + roomId + " not found or inactive",
                            "ROOM_NOT_FOUND", "roomIds", "roomId=" + roomId);
                }
                if (bookingRepository.isRoomBooked(roomId, req.getCheckInDate(), req.getCheckOutDate())) {
                    return StandardResponse.error(
                            "Room " + room.getRoomNumber() + " is not available for selected dates",
                            "ROOM_UNAVAILABLE", "roomIds", "roomId=" + roomId);
                }
                rooms.add(room);
            }

            // 5. Build Reservation
            Reservation reservation = Reservation.builder()
                    .guest(guest)
                    .hotel(rooms.get(0).getFloor().getHotel())
                    .checkInDate(req.getCheckInDate())
                    .checkInTime(req.getCheckInTime() != null ? req.getCheckInTime() : LocalTime.of(14, 0))
                    .checkOutDate(req.getCheckOutDate())
                    .checkOutTime(req.getCheckOutTime() != null ? req.getCheckOutTime() : LocalTime.of(11, 0))
                    .numberOfNights((int) nights)
                    .numberOfAdults(req.getNumberOfAdults())
                    .numberOfChildren(req.getNumberOfChildren() != null ? req.getNumberOfChildren() : 0)
                    .numberOfRooms(rooms.size())
                    .reservationStatus(req.getReservationStatus() != null
                            ? req.getReservationStatus()
                            : Reservation.ReservationStatus.CONFIRMED)
                    .ratePlan(req.getRatePlan())
                    .billingName(req.getBillingName())
                    .billingAddress(req.getBillingAddress())
                    .specialRequests(req.getSpecialRequests())
                    .notes(req.getNotes())
                    .isDeleted(false)
                    .build();

            Reservation savedReservation = reservationRepository.save(reservation);

            // 6. Build one Booking per room
            BigDecimal ratePlanCharge = RATE_PLAN_CHARGES.getOrDefault(req.getRatePlan(), BigDecimal.ZERO);
            List<Booking> bookings = new ArrayList<>();

            for (Room room : rooms) {
                BigDecimal ratePerNight  = room.getRoomType().getBasePricePerNight();
                BigDecimal effectiveRate = ratePerNight.add(ratePlanCharge);
                BigDecimal total         = effectiveRate.multiply(BigDecimal.valueOf(nights));

                Booking booking = Booking.builder()
                        .reservation(savedReservation)
                        .room(room)
                        .checkInDate(req.getCheckInDate())
                        .checkOutDate(req.getCheckOutDate())
                        .numberOfNights((int) nights)
                        .ratePerNight(ratePerNight)
                        .ratePlanCharge(ratePlanCharge)
                        .totalPrice(total)
                        .discountPercentage(BigDecimal.ZERO)
                        .discountAmount(BigDecimal.ZERO)
                        .finalPrice(total)
                        .bookingStatus(Booking.BookingStatus.CONFIRMED)
                        .isDeleted(false)
                        .build();

                bookings.add(booking);
            }

            List<Booking> savedBookings = bookingRepository.saveAll(bookings);
            savedReservation.setBookings(savedBookings);

            // Write Room Audit Logs for Reservation Creation
            for (Booking sb : savedBookings) {
                RoomAudit audit = RoomAudit.builder()
                        .room(sb.getRoom())
                        .booking(sb)
                        .operationType("RESERVATION")
                        .amountPaid(BigDecimal.ZERO)
                        .notes("Room reserved via Reservation #" + savedReservation.getId() + " by guest " + guest.getFirstName() + " " + guest.getLastName())
                        .createdAt(LocalDateTime.now())
                        .build();
                roomAuditRepository.save(audit);
            }

            log.info("Reservation created id={}, bookings={}", savedReservation.getId(), savedBookings.size());
            return StandardResponse.success(mapToResponse(savedReservation), "Reservation created successfully");

        } catch (Exception e) {
            log.error("Error creating reservation: ", e);
            return StandardResponse.error("Failed to create reservation", "CREATE_ERROR", null, e.getMessage());
        }
    }

    // ── Read ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getReservationById(Long id) {
        log.info("Fetching reservation id={}", id);
        try {
            return reservationRepository.findByIdAndIsDeletedFalse(id)
                    .map(r -> StandardResponse.success(
                            mapToDetailResponse(r),
                            "Reservation fetched successfully"))
                    .orElseGet(() -> StandardResponse.error(
                            "Reservation not found",
                            "NOT_FOUND",
                            "id",
                            null
                    ));
        } catch (Exception e) {
            log.error("Error fetching reservation id={}: ", id, e);
            return StandardResponse.error(
                    "Failed to fetch reservation",
                    "FETCH_ERROR",
                    null,
                    e.getMessage()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getAllReservations(String search) {
        log.info("Fetching all reservations, search={}", search);
        try {
            List<Reservation> list = (search != null && !search.isBlank())
                    ? reservationRepository.searchReservations(search.trim())
                    : reservationRepository.findByIsDeletedFalse();

            List<ReservationResponse> responses = list.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            StandardResponse.ResponseMetadata meta = StandardResponse.ResponseMetadata.builder()
                    .totalRecords((long) responses.size())
                    .operation("GET_ALL_RESERVATIONS")
                    .build();

            return StandardResponse.success(responses, "Reservations fetched successfully", meta);
        } catch (Exception e) {
            log.error("Error fetching reservations: ", e);
            return StandardResponse.error("Failed to fetch reservations", "FETCH_ERROR", null, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getReservationsByGuest(Long guestId) {
        log.info("Fetching reservations for guestId={}", guestId);
        try {
            List<ReservationResponse> list = reservationRepository
                    .findByGuest_IdAndIsDeletedFalse(guestId)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            return StandardResponse.success(list, "Guest reservations fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching reservations for guest {}: ", guestId, e);
            return StandardResponse.error("Failed to fetch guest reservations", "FETCH_ERROR", null, e.getMessage());
        }
    }

    // ── Cancel ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<?> cancelReservation(Long id) {
        log.info("Cancelling reservation id={}", id);
        try {
            Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(id).orElse(null);
            if (reservation == null) {
                return StandardResponse.error("Reservation not found", "NOT_FOUND", "id", null);
            }

            reservation.setReservationStatus(Reservation.ReservationStatus.CANCELLED);
            reservation.getBookings().forEach(b -> {
                b.setBookingStatus(Booking.BookingStatus.CANCELLED);
                
                RoomAudit audit = RoomAudit.builder()
                        .room(b.getRoom())
                        .booking(b)
                        .operationType("CANCEL_RESERVATION")
                        .amountPaid(BigDecimal.ZERO)
                        .notes("Reservation Cancelled for Room " + b.getRoom().getRoomNumber())
                        .createdAt(LocalDateTime.now())
                        .build();
                roomAuditRepository.save(audit);
            });
            reservationRepository.save(reservation);

            log.info("Reservation id={} cancelled successfully", id);
            return StandardResponse.success("Reservation cancelled successfully");
        } catch (Exception e) {
            log.error("Error cancelling reservation id={}: ", id, e);
            return StandardResponse.error("Failed to cancel reservation", "CANCEL_ERROR", null, e.getMessage());
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StandardResponse<?> deleteReservation(Long id) {
        log.info("Soft-deleting reservation id={}", id);
        try {
            Reservation reservation = reservationRepository.findByIdAndIsDeletedFalse(id).orElse(null);
            if (reservation == null) {
                return StandardResponse.error("Reservation not found", "NOT_FOUND", "id", null);
            }

            reservation.setIsDeleted(true);
            reservation.getBookings().forEach(b -> b.setIsDeleted(true));
            reservationRepository.save(reservation);

            log.info("Reservation id={} deleted successfully", id);
            return StandardResponse.success("Reservation deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting reservation id={}: ", id, e);
            return StandardResponse.error("Failed to delete reservation", "DELETE_ERROR", null, e.getMessage());
        }
    }

    // ── Available Rooms ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getAvailableRooms(Long floorId, LocalDate checkIn, LocalDate checkOut) {
        log.info("Fetching available rooms hotelId={}, {} to {}", floorId, checkIn, checkOut);
        try {
            if (checkOut == null || checkIn == null || !checkOut.isAfter(checkIn)) {
                return StandardResponse.error("Valid check-in and check-out dates are required",
                        "INVALID_DATES", "checkIn/checkOut", null);
            }

            List<Room> rooms = roomRepository.findAvailableRooms(floorId, checkIn, checkOut);
            List<RoomResponse> responses = rooms.stream()
                    .map(this::mapRoomToResponse)
                    .collect(Collectors.toList());

            StandardResponse.ResponseMetadata meta = StandardResponse.ResponseMetadata.builder()
                    .totalRecords((long) responses.size())
                    .operation("GET_AVAILABLE_ROOMS")
                    .build();

            return StandardResponse.success(responses, "Available rooms fetched successfully", meta);
        } catch (Exception e) {
            log.error("Error fetching available rooms: ", e);
            return StandardResponse.error("Failed to fetch available rooms", "FETCH_ERROR", null, e.getMessage());
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    // ── Listing mapper ────────────────────────────────────────────────────
    /** Maps to slim listing DTO — only what the listing columns show. */
    private ReservationResponse mapToResponse(Reservation r) {
        List<Booking> bookings = r.getBookings() != null ? r.getBookings() : List.of();

        BigDecimal grandTotal = bookings.stream().map(Booking::getFinalPrice)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidAmount = BigDecimal.ZERO;
        if (r.getId() != null) {
            try {
                paidAmount = billRepository.sumPaidAmountByReservation(r.getId());
                if (paidAmount == null) paidAmount = BigDecimal.ZERO;
            } catch (Exception ignored) {}
        }

        Guest g = r.getGuest();

        List<ReservationResponse.RoomSummary> roomSummaries = bookings.stream()
                .map(b -> ReservationResponse.RoomSummary.builder()
                        .roomNumber(b.getRoom().getRoomNumber())
                        .roomTypeName(b.getRoom().getRoomType() != null
                                ? b.getRoom().getRoomType().getName() : null)
                        .ratePlanCode(r.getRatePlan() != null ? r.getRatePlan().name() : null)
                        .build())
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .id(r.getId())
                .guestId(g.getId())
                .guestInitials(extractInitials(g.getFirstName(), g.getLastName()))
                .guestFullName(g.getFirstName() + " " + g.getLastName())
                .guestPhone(g.getPhone())
                .guestBadge(resolveGuestBadge(g))
                .checkInDate(r.getCheckInDate())
                .checkOutDate(r.getCheckOutDate())
                .numberOfNights(r.getNumberOfNights())
                .numberOfAdults(r.getNumberOfAdults())
                .numberOfChildren(r.getNumberOfChildren())
                .reservationStatus(r.getReservationStatus())
                .numberOfRooms(r.getNumberOfRooms())
                .rooms(roomSummaries)
                .grandTotal(grandTotal)
                .paidAmount(paidAmount)
                .build();
    }

    // ── Detail mapper ─────────────────────────────────────────────────────
    /** Maps to full detail DTO — used by getReservationById only. */
    private ReservationDetailResponse mapToDetailResponse(Reservation r) {
        List<Booking> bookings = r.getBookings() != null ? r.getBookings() : List.of();

        BigDecimal totalPrice    = bookings.stream().map(Booking::getTotalPrice)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = bookings.stream().map(Booking::getDiscountAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal    = bookings.stream().map(Booking::getFinalPrice)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidAmount = BigDecimal.ZERO;
        if (r.getId() != null) {
            try {
                paidAmount = billRepository.sumPaidAmountByReservation(r.getId());
                if (paidAmount == null) paidAmount = BigDecimal.ZERO;
            } catch (Exception ignored) {}
        }

        Guest g = r.getGuest();
        return ReservationDetailResponse.builder()
                .id(r.getId())
                .guestId(g.getId())
                .guestInitials(extractInitials(g.getFirstName(), g.getLastName()))
                .guestFullName(g.getFirstName() + " " + g.getLastName())
                .guestEmail(g.getEmail())
                .guestPhone(g.getPhone())
                .guestIsVip(g.getIsVip())
                .guestBadge(resolveGuestBadge(g))
                .hotelId(r.getHotel() != null ? r.getHotel().getId() : null)
                .hotelName(r.getHotel() != null ? r.getHotel().getName() : null)
                .checkInDate(r.getCheckInDate())
                .checkInTime(r.getCheckInTime())
                .checkOutDate(r.getCheckOutDate())
                .checkOutTime(r.getCheckOutTime())
                .numberOfNights(r.getNumberOfNights())
                .numberOfAdults(r.getNumberOfAdults())
                .numberOfChildren(r.getNumberOfChildren())
                .reservationStatus(r.getReservationStatus())
                .ratePlan(r.getRatePlan())
                .numberOfRooms(r.getNumberOfRooms())
                .bookings(bookings.stream().map(this::mapBookingToResponse).collect(Collectors.toList()))
                .billingName(r.getBillingName())
                .billingAddress(r.getBillingAddress())
                .totalPrice(totalPrice)
                .totalDiscount(totalDiscount)
                .grandTotal(grandTotal)
                .paidAmount(paidAmount)
                .specialRequests(r.getSpecialRequests())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    /** Extract up to 2 uppercase initials from first + last name. */
    private String extractInitials(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) sb.append(firstName.charAt(0));
        if (lastName  != null && !lastName.isBlank())  sb.append(lastName.charAt(0));
        return sb.toString().toUpperCase();
    }

    /** Resolve guest badge: VIP → REPEAT (>1 reservation) → NEW */
    private String resolveGuestBadge(Guest g) {
        if (Boolean.TRUE.equals(g.getIsVip())) return "VIP";
        long count = reservationRepository.findByGuest_IdAndIsDeletedFalse(g.getId()).size();
        return count > 1 ? "REPEAT" : "NEW";
    }

    private BookingResponse mapBookingToResponse(Booking b) {
        Room room = b.getRoom();
        return BookingResponse.builder()
                .id(b.getId())
                .reservationId(b.getReservation() != null ? b.getReservation().getId() : null)
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomTypeName(room.getRoomType() != null ? room.getRoomType().getName() : null)
                .floor(room.getFloor().getFloorNumber())
                .checkInDate(b.getCheckInDate())
                .checkOutDate(b.getCheckOutDate())
                .numberOfNights(b.getNumberOfNights())
                .ratePerNight(b.getRatePerNight())
                .ratePlanCharge(b.getRatePlanCharge())
                .totalPrice(b.getTotalPrice())
                .discountPercentage(b.getDiscountPercentage())
                .discountAmount(b.getDiscountAmount())
                .finalPrice(b.getFinalPrice())
                .bookingStatus(b.getBookingStatus())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private RoomResponse mapRoomToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor().getFloorNumber())
                .roomTypeId(room.getRoomType() != null ? room.getRoomType().getId() : null)
                .roomTypeName(room.getRoomType() != null ? room.getRoomType().getName() : null)
                .basePricePerNight(room.getRoomType() != null ? room.getRoomType().getBasePricePerNight() : null)
                .maxOccupancy(room.getMaxOccupancy())
                .status(room.getStatus())
                .isActive(room.getIsActive())
                .build();
    }

    // ── Inline Guest Builder ───────────────────────────────────────────────

    /**
     * Constructs a new {@link Guest} entity from the inline guest details
     * provided inside a {@link ReservationRequest}.
     * Used only by the "Create Guest" flow of createReservation.
     */
    private Guest buildInlineGuest(GuestRequest gd) {
        LocalDateTime now = LocalDateTime.now();
        return Guest.builder()
                .title(gd.getTitle())
                .firstName(gd.getFirstName())
                .lastName(gd.getLastName())
                .countryCode(gd.getCountryCode())
                .phone(gd.getPhone())
                .email(gd.getEmail())
                .addressLine1(gd.getAddressLine1())
                .addressLine2(gd.getAddressLine2())
                .city(gd.getCity())
                .state(gd.getState())
                .postCode(gd.getPostCode())
                .country(gd.getCountry())
                .nationality(gd.getNationality())
                .gender(gd.getGender())
                .dateOfBirth(gd.getDateOfBirth())
                .idProofType(gd.getIdProofType())
                .idProofNumber(gd.getIdProofNumber())
                .guestNotes(gd.getGuestNotes())
                .preference(gd.getPreference())
                .isVip(gd.getIsVip() != null ? gd.getIsVip() : false)
                .isActive(true)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getArrivals(LocalDate date, String search, boolean checkout) {
        log.info("Fetching listing date={}, search={}, checkout={}", date, search, checkout);
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            List<Booking> allBookings = bookingRepository.findByIsDeletedFalse();
            
            // Filter by date
            List<Booking> dayBookings = allBookings.stream()
                    .filter(b -> checkout ? targetDate.equals(b.getCheckOutDate()) : targetDate.equals(b.getCheckInDate()))
                    .collect(Collectors.toList());

            // Compute counts
            long pendingCount;
            long processedCount;
            if (checkout) {
                pendingCount = dayBookings.stream()
                        .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CHECKED_IN)
                        .count();
                processedCount = dayBookings.stream()
                        .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT)
                        .count();
            } else {
                pendingCount = dayBookings.stream()
                        .filter(b -> b.getBookingStatus() == Booking.BookingStatus.PENDING 
                                || b.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                        .count();
                processedCount = dayBookings.stream()
                        .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CHECKED_IN)
                        .count();
            }

            long totalCount = pendingCount + processedCount;

            // Apply search filter if provided
            List<Booking> filteredBookings = dayBookings;
            if (search != null && !search.trim().isEmpty()) {
                String query = search.trim().toLowerCase();
                filteredBookings = dayBookings.stream()
                        .filter(b -> {
                            Guest g = b.getReservation().getGuest();
                            String fullName = (g.getFirstName() + " " + g.getLastName()).toLowerCase();
                            String bookingRef = ("bk-" + b.getId()).toLowerCase();
                            String roomNum = b.getRoom() != null ? b.getRoom().getRoomNumber().toLowerCase() : "";
                            return fullName.contains(query) || bookingRef.contains(query) 
                                    || b.getId().toString().contains(query) || roomNum.contains(query);
                        })
                        .collect(Collectors.toList());
            }

            // Map to ArrivalBookingResponse
            List<ArrivalBookingResponse> arrivals = filteredBookings.stream()
                    .map(b -> {
                        Guest g = b.getReservation().getGuest();
                        
                        // Calculate Balance
                        BigDecimal totalCharges;
                        BigDecimal paidAmount = BigDecimal.ZERO;
                        Optional<Bill> optBill = billRepository.findByBooking_Id(b.getId());
                        if (optBill.isPresent()) {
                            Bill bill = optBill.get();
                            totalCharges = bill.getTotalAmount();
                            paidAmount = paymentRepository.findByBill_Id(bill.getId()).stream()
                                    .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                                    .map(Payment::getAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                        } else {
                            BigDecimal roomCharges = b.getFinalPrice();
                            BigDecimal taxAmount = roomCharges.multiply(new BigDecimal("0.18"));
                            totalCharges = roomCharges.add(taxAmount);
                        }
                        BigDecimal balance = totalCharges.subtract(paidAmount);

                        String statusStr;
                        if (checkout) {
                            if (b.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                                statusStr = "Pending";
                            } else if (b.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT) {
                                statusStr = "Checked Out";
                            } else {
                                statusStr = b.getBookingStatus().name();
                            }
                        } else {
                            if (b.getBookingStatus() == Booking.BookingStatus.PENDING 
                                    || b.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
                                statusStr = "Pending";
                            } else if (b.getBookingStatus() == Booking.BookingStatus.CHECKED_IN) {
                                statusStr = "Checked In";
                            } else {
                                statusStr = b.getBookingStatus().name();
                            }
                        }

                        return ArrivalBookingResponse.builder()
                                .bookingId(b.getId())
                                .bookingRef("BK-" + b.getId())
                                .guestName(g.getFirstName() + " " + g.getLastName())
                                .guestIsVip(g.getIsVip())
                                .numberOfNights(b.getNumberOfNights())
                                .roomTypeName(b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().getName() : "")
                                .eta(checkout ? b.getReservation().getCheckOutTime() : b.getReservation().getCheckInTime())
                                .balance(balance)
                                .bookingStatus(statusStr)
                                .checkInDate(b.getCheckInDate())
                                .checkOutDate(b.getCheckOutDate())
                                .roomNumber(b.getRoom() != null ? b.getRoom().getRoomNumber() : null)
                                .build();
                    })
                    .collect(Collectors.toList());

            ArrivalsListResponse response = ArrivalsListResponse.builder()
                    .arrivals(arrivals)
                    .pendingArrivalsCount(pendingCount)
                    .checkedInCount(processedCount)
                    .totalExpectedCount(totalCount)
                    .build();

            return StandardResponse.success(response, checkout ? "Departures fetched successfully" : "Arrivals fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching arrivals/departures: ", e);
            return StandardResponse.error("Failed to fetch list", "FETCH_LIST_ERROR", null, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getCheckInDetails(Long bookingId) {
        log.info("Fetching checkin details for bookingId={}", bookingId);
        try {
            Booking b = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
            Reservation res = b.getReservation();
            Guest g = res.getGuest();
            
            // Map Rate Plan to a descriptive string
            String ratePlanName = res.getRatePlan() != null ? res.getRatePlan().name() : "";
            if (res.getRatePlan() == Reservation.RatePlan.EP) ratePlanName = "Room Only (EP)";
            else if (res.getRatePlan() == Reservation.RatePlan.CP) ratePlanName = "Breakfast Included (CP)";
            else if (res.getRatePlan() == Reservation.RatePlan.MAP) ratePlanName = "Half Board (MAP)";
            else if (res.getRatePlan() == Reservation.RatePlan.AP) ratePlanName = "Full Board (AP)";

            // Occupancy string
            String occupancy = res.getNumberOfAdults() + " Adults";
            if (res.getNumberOfChildren() != null && res.getNumberOfChildren() > 0) {
                occupancy += ", " + res.getNumberOfChildren() + " Children";
            }

            // Estimate Bill & Balance Due
            BigDecimal roomCharges = b.getFinalPrice();
            BigDecimal taxAmount = roomCharges.multiply(new BigDecimal("0.18"));
            BigDecimal totalEstBill = roomCharges.add(taxAmount);
            
            BigDecimal paidAmount = BigDecimal.ZERO;
            Optional<Bill> optBill = billRepository.findByBooking_Id(bookingId);
            if (optBill.isPresent()) {
                paidAmount = paymentRepository.findByBill_Id(optBill.get().getId()).stream()
                        .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            BigDecimal balanceDue = totalEstBill.subtract(paidAmount);

            // Available rooms of the same type
            List<Room> vacantRooms = roomRepository.findAvailableRooms(res.getHotel().getId(), b.getCheckInDate(), b.getCheckOutDate());
            List<AvailableRoomSummary> availableRooms = vacantRooms.stream()
                    .filter(r -> r.getRoomType() != null && b.getRoom().getRoomType() != null 
                            && r.getRoomType().getId().equals(b.getRoom().getRoomType().getId()))
                    .map(r -> AvailableRoomSummary.builder()
                            .roomId(r.getId())
                            .roomNumber(r.getRoomNumber())
                            .build())
                    .collect(Collectors.toList());

            // Ensure the currently assigned room is in the list
            if (b.getRoom() != null) {
                boolean exists = availableRooms.stream().anyMatch(ar -> ar.getRoomId().equals(b.getRoom().getId()));
                if (!exists) {
                    availableRooms.add(0, AvailableRoomSummary.builder()
                            .roomId(b.getRoom().getId())
                            .roomNumber(b.getRoom().getRoomNumber())
                            .build());
                }
            }

            CheckInPopupResponse response = CheckInPopupResponse.builder()
                    .bookingId(b.getId())
                    .bookingRef("BK-" + b.getId())
                    .guestName(g.getFirstName() + " " + g.getLastName())
                    .guestPhone(g.getPhone())
                    .guestIsVip(g.getIsVip())
                    .checkInDate(b.getCheckInDate())
                    .expectedArrival(res.getCheckInTime())
                    .numberOfNights(b.getNumberOfNights())
                    .roomTypeName(b.getRoom().getRoomType() != null ? b.getRoom().getRoomType().getName() : "")
                    .occupancy(occupancy)
                    .ratePlan(ratePlanName)
                    .source("Direct Booking")
                    .totalEstBill(totalEstBill)
                    .balanceDue(balanceDue)
                    .assignedRoomNumber(b.getRoom() != null ? b.getRoom().getRoomNumber() : null)
                    .assignedRoomId(b.getRoom() != null ? b.getRoom().getId() : null)
                    .availableRooms(availableRooms)
                    .build();

            return StandardResponse.success(response, "Check-in details fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching check-in details: ", e);
            return StandardResponse.error("Failed to fetch check-in details", "FETCH_CHECKIN_DETAILS_ERROR", null, e.getMessage());
        }
    }

    @Override
    @Transactional
    public StandardResponse<?> completeCheckIn(CheckInRequest request) {
        log.info("Completing check-in for request={}", request);
        try {
            if (request.getBookingId() == null) {
                return StandardResponse.error("Booking ID is required", "BOOKING_ID_REQUIRED", "bookingId", null);
            }
            if (request.getRoomId() == null) {
                return StandardResponse.error("Room ID is required", "ROOM_ID_REQUIRED", "roomId", null);
            }

            Booking b = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + request.getBookingId()));

            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + request.getRoomId()));

            // Update booking's room & status
            b.setRoom(room);
            b.setBookingStatus(Booking.BookingStatus.CHECKED_IN);
            b.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(b);

            // Update room status
            room.setStatus(Room.RoomStatus.OCCUPIED);
            room.setUpdatedAt(LocalDateTime.now());
            roomRepository.save(room);

            // Update parent reservation status if all bookings are checked in
            Reservation res = b.getReservation();
            List<Booking> bookings = bookingRepository.findByReservation_IdAndIsDeletedFalse(res.getId());
            boolean allCheckedIn = bookings.stream()
                    .allMatch(bk -> bk.getBookingStatus() == Booking.BookingStatus.CHECKED_IN 
                            || bk.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT);
            if (allCheckedIn) {
                res.setReservationStatus(Reservation.ReservationStatus.CHECKED_IN);
                res.setUpdatedAt(LocalDateTime.now());
                reservationRepository.save(res);
            }

            // Create or get Bill
            Bill bill = billRepository.findByBooking_Id(b.getId()).orElse(null);
            if (bill == null) {
                BigDecimal roomCharges = b.getFinalPrice();
                BigDecimal taxAmount = roomCharges.multiply(new BigDecimal("0.18"));
                BigDecimal totalAmount = roomCharges.add(taxAmount);

                bill = Bill.builder()
                        .booking(b)
                        .guest(res.getGuest())
                        .roomCharges(roomCharges)
                        .taxAmount(taxAmount)
                        .totalAmount(totalAmount)
                        .additionalCharges(BigDecimal.ZERO)
                        .billStatus(Bill.BillStatus.ISSUED)
                        .billDate(LocalDate.now())
                        .paymentDueDate(b.getCheckOutDate())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                bill = billRepository.save(bill);
            }

            // Save money transaction if any
            if (request.getAmountToSettle() != null && request.getAmountToSettle().compareTo(BigDecimal.ZERO) > 0) {
                Payment payment = Payment.builder()
                        .bill(bill)
                        .amount(request.getAmountToSettle())
                        .paymentMode(mapPaymentMode(request.getPaymentMethod()))
                        .paymentDate(LocalDate.now())
                        .transactionId("TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000))
                        .paymentStatus(Payment.PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .build();
                paymentRepository.save(payment);

                // Recalculate bill status
                BigDecimal totalPaid = paymentRepository.findByBill_Id(bill.getId()).stream()
                        .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalPaid.compareTo(bill.getTotalAmount()) >= 0) {
                    bill.setBillStatus(Bill.BillStatus.PAID);
                } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                    bill.setBillStatus(Bill.BillStatus.PARTIALLY_PAID);
                } else {
                    bill.setBillStatus(Bill.BillStatus.ISSUED);
                }
                bill.setUpdatedAt(LocalDateTime.now());
                billRepository.save(bill);
            }

            // Write Room Audit Log for Check-In
            BigDecimal amtPaid = request.getAmountToSettle() != null ? request.getAmountToSettle() : BigDecimal.ZERO;
            RoomAudit audit = RoomAudit.builder()
                    .room(room)
                    .booking(b)
                    .operationType("CHECK_IN")
                    .amountPaid(amtPaid)
                    .notes("Guest checked in to Room " + room.getRoomNumber() + ". Payment Mode: " + request.getPaymentMethod())
                    .createdAt(LocalDateTime.now())
                    .build();
            roomAuditRepository.save(audit);

            return StandardResponse.success(null, "Checked in successfully");
        } catch (Exception e) {
            log.error("Error completing check-in: ", e);
            return StandardResponse.error("Failed to complete check-in", "CHECKIN_ERROR", null, e.getMessage());
        }
    }

    private Payment.PaymentMode mapPaymentMode(String mode) {
        if (mode == null) return Payment.PaymentMode.CASH;
        String normalized = mode.trim().toUpperCase().replace(" ", "_");
        try {
            return Payment.PaymentMode.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return Payment.PaymentMode.CASH;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getFolio(Long bookingId) {
        log.info("Fetching folio for bookingId={}", bookingId);
        try {
            Booking b = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
            Reservation res = b.getReservation();
            Guest g = res.getGuest();
            Room room = b.getRoom();

            List<FolioTransaction> transactions = new ArrayList<>();
            BigDecimal totalCharges = BigDecimal.ZERO;
            BigDecimal totalPayments = BigDecimal.ZERO;

            Optional<Bill> optBill = billRepository.findByBooking_Id(bookingId);
            if (optBill.isPresent()) {
                Bill bill = optBill.get();
                totalCharges = bill.getTotalAmount();

                // Add Room Charges
                transactions.add(FolioTransaction.builder()
                        .date(bill.getBillDate() != null ? bill.getBillDate() : b.getCheckInDate())
                        .description("Room Charge (" + room.getRoomType().getName() + ")")
                        .charges(bill.getRoomCharges())
                        .payments(null)
                        .type("CHARGE")
                        .build());

                // Add GST
                transactions.add(FolioTransaction.builder()
                        .date(bill.getBillDate() != null ? bill.getBillDate() : b.getCheckInDate())
                        .description("GST (18%)")
                        .charges(bill.getTaxAmount())
                        .payments(null)
                        .type("CHARGE")
                        .build());

                // Add Additional Charges if present
                if (bill.getAdditionalCharges() != null && bill.getAdditionalCharges().compareTo(BigDecimal.ZERO) > 0) {
                    transactions.add(FolioTransaction.builder()
                            .date(bill.getBillDate() != null ? bill.getBillDate() : b.getCheckInDate())
                            .description("Additional Charges")
                            .charges(bill.getAdditionalCharges())
                            .payments(null)
                            .type("CHARGE")
                            .build());
                }

                // Add payments
                List<Payment> payments = paymentRepository.findByBill_Id(bill.getId());
                for (Payment p : payments) {
                    if (p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
                        totalPayments = totalPayments.add(p.getAmount());
                        
                        // Format description to match screenshots
                        String desc = "Payment Received";
                        if (p.getPaymentDate() != null && p.getPaymentDate().isBefore(b.getCheckInDate())) {
                            desc = "Advance Payment (" + formatPaymentMode(p.getPaymentMode()) + ")";
                        } else if (p.getNotes() != null && p.getNotes().toLowerCase().contains("advance")) {
                            desc = "Advance Payment (" + formatPaymentMode(p.getPaymentMode()) + ")";
                        }

                        transactions.add(FolioTransaction.builder()
                                .date(p.getPaymentDate() != null ? p.getPaymentDate() : LocalDate.now())
                                .description(desc)
                                .charges(null)
                                .payments(p.getAmount())
                                .type("PAYMENT")
                                .build());
                    }
                }
            } else {
                // Draft Folio (Bill doesn't exist yet)
                BigDecimal roomCharges = b.getFinalPrice();
                BigDecimal taxAmount = roomCharges.multiply(new BigDecimal("0.18"));
                totalCharges = roomCharges.add(taxAmount);

                transactions.add(FolioTransaction.builder()
                        .date(b.getCheckInDate())
                        .description("Room Charge (" + room.getRoomType().getName() + ")")
                        .charges(roomCharges)
                        .payments(null)
                        .type("CHARGE")
                        .build());

                transactions.add(FolioTransaction.builder()
                        .date(b.getCheckInDate())
                        .description("GST (18%)")
                        .charges(taxAmount)
                        .payments(null)
                        .type("CHARGE")
                        .build());
            }

            BigDecimal currentBalance = totalCharges.subtract(totalPayments);

            FolioResponse response = FolioResponse.builder()
                    .bookingRef("BK-" + b.getId())
                    .guestName(g.getFirstName() + " " + g.getLastName())
                    .guestPhone(g.getPhone())
                    .roomNumber(room != null ? "Room " + room.getRoomNumber() : "Not Assigned")
                    .transactions(transactions)
                    .totalCharges(totalCharges)
                    .totalPayments(totalPayments)
                    .currentBalance(currentBalance)
                    .build();

            return StandardResponse.success(response, "Folio fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching folio: ", e);
            return StandardResponse.error("Failed to fetch folio", "FETCH_FOLIO_ERROR", null, e.getMessage());
        }
    }

    @Override
    @Transactional
    public StandardResponse<?> completeCheckOut(CheckOutRequest request) {
        log.info("Completing check-out for request={}", request);
        try {
            if (request.getBookingId() == null) {
                return StandardResponse.error("Booking ID is required", "BOOKING_ID_REQUIRED", "bookingId", null);
            }

            Booking b = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + request.getBookingId()));

            if (b.getBookingStatus() != Booking.BookingStatus.CHECKED_IN) {
                return StandardResponse.error("Booking is not in CHECKED_IN status", "INVALID_STATUS", "bookingStatus", null);
            }

            Room room = b.getRoom();

            // Update booking status
            b.setBookingStatus(Booking.BookingStatus.CHECKED_OUT);
            b.setUpdatedAt(LocalDateTime.now());
            
            // Append checkout details/feedback/transportation to notes for audit
            StringBuilder notesBuilder = new StringBuilder();
            if (b.getReservation().getNotes() != null) {
                notesBuilder.append(b.getReservation().getNotes()).append("\n");
            }
            notesBuilder.append("[Checkout Audit] Keys Returned: ").append(request.getKeysReturned() != null ? request.getKeysReturned() : "N/A");
            if (request.getTransportationRequested() != null && !request.getTransportationRequested().isEmpty()) {
                notesBuilder.append(", Transportation: ").append(request.getTransportationRequested());
            }
            if (request.getGuestFeedback() != null && !request.getGuestFeedback().isEmpty()) {
                notesBuilder.append(", Feedback: ").append(request.getGuestFeedback());
            }
            if (request.getDamageDescription() != null && !request.getDamageDescription().isEmpty()) {
                notesBuilder.append(", Damage Description: ").append(request.getDamageDescription());
            }
            b.getReservation().setNotes(notesBuilder.toString());
            b.getReservation().setUpdatedAt(LocalDateTime.now());
            reservationRepository.save(b.getReservation());
            bookingRepository.save(b);

            // Update Room status to VACANT
            if (room != null) {
                room.setStatus(Room.RoomStatus.VACANT);
                room.setUpdatedAt(LocalDateTime.now());
                roomRepository.save(room);
            }

            // Update reservation status to CHECKED_OUT if all bookings under this reservation are CHECKED_OUT
            Reservation res = b.getReservation();
            List<Booking> bookings = bookingRepository.findByReservation_IdAndIsDeletedFalse(res.getId());
            boolean allCheckedOut = bookings.stream()
                    .allMatch(bk -> bk.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT);
            if (allCheckedOut) {
                res.setReservationStatus(Reservation.ReservationStatus.CHECKED_OUT);
                res.setUpdatedAt(LocalDateTime.now());
                reservationRepository.save(res);
            }

            // Retrieve or create Bill
            Bill bill = billRepository.findByBooking_Id(b.getId()).orElse(null);
            if (bill == null) {
                BigDecimal roomCharges = b.getFinalPrice();
                BigDecimal taxAmount = roomCharges.multiply(new BigDecimal("0.18"));
                BigDecimal totalAmount = roomCharges.add(taxAmount);

                bill = Bill.builder()
                        .booking(b)
                        .guest(res.getGuest())
                        .roomCharges(roomCharges)
                        .taxAmount(taxAmount)
                        .totalAmount(totalAmount)
                        .additionalCharges(BigDecimal.ZERO)
                        .billStatus(Bill.BillStatus.ISSUED)
                        .billDate(LocalDate.now())
                        .paymentDueDate(b.getCheckOutDate())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                bill = billRepository.save(bill);
            }

            // Update additional charges in the bill (late fee, minibar, damage penalty)
            BigDecimal lateFee = request.getLateCheckOutFee() != null ? request.getLateCheckOutFee() : BigDecimal.ZERO;
            BigDecimal minibar = request.getMinibarCharges() != null ? request.getMinibarCharges() : BigDecimal.ZERO;
            BigDecimal damage = request.getDamagePenaltyCharge() != null ? request.getDamagePenaltyCharge() : BigDecimal.ZERO;
            BigDecimal newAdditional = lateFee.add(minibar).add(damage);

            if (newAdditional.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentAdditional = bill.getAdditionalCharges() != null ? bill.getAdditionalCharges() : BigDecimal.ZERO;
                bill.setAdditionalCharges(currentAdditional.add(newAdditional));
                bill.setTotalAmount(bill.getRoomCharges().add(bill.getTaxAmount()).add(bill.getAdditionalCharges()));
            }

            // Save checkout payment if any
            if (request.getAmountToCollect() != null && request.getAmountToCollect().compareTo(BigDecimal.ZERO) > 0) {
                Payment payment = Payment.builder()
                        .bill(bill)
                        .amount(request.getAmountToCollect())
                        .paymentMode(mapPaymentMode(request.getPaymentMethod()))
                        .paymentDate(LocalDate.now())
                        .transactionId("TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000))
                        .paymentStatus(Payment.PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .build();
                paymentRepository.save(payment);
            }

            // Recalculate bill status based on all payments
            BigDecimal totalPaid = paymentRepository.findByBill_Id(bill.getId()).stream()
                    .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalPaid.compareTo(bill.getTotalAmount()) >= 0) {
                bill.setBillStatus(Bill.BillStatus.PAID);
            } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                bill.setBillStatus(Bill.BillStatus.PARTIALLY_PAID);
            } else {
                bill.setBillStatus(Bill.BillStatus.ISSUED);
            }
            bill.setUpdatedAt(LocalDateTime.now());
            billRepository.save(bill);

            // Write Room Audit Log for Check-Out
            BigDecimal checkoutPaid = request.getAmountToCollect() != null ? request.getAmountToCollect() : BigDecimal.ZERO;
            if (room != null) {
                RoomAudit audit = RoomAudit.builder()
                        .room(room)
                        .booking(b)
                        .operationType("CHECK_OUT")
                        .amountPaid(checkoutPaid)
                        .notes("Guest checked out from Room " + room.getRoomNumber() + ". Keys Returned: " + request.getKeysReturned())
                        .createdAt(LocalDateTime.now())
                        .build();
                roomAuditRepository.save(audit);
            }

            return StandardResponse.success(null, "Checked out successfully");
        } catch (Exception e) {
            log.error("Error completing check-out: ", e);
            return StandardResponse.error("Failed to complete check-out", "CHECKOUT_ERROR", null, e.getMessage());
        }
    }

    private String formatPaymentMode(Payment.PaymentMode mode) {
        if (mode == null) return "Cash";
        String name = mode.name().toLowerCase().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String s : name.split(" ")) {
            if (!s.isEmpty()) {
                sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getRoomAudits(Long roomId) {
        log.info("Fetching room audit logs for roomId={}", roomId);
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + roomId));

            List<RoomAudit> audits = roomAuditRepository.findByRoom_Id(roomId);
            List<RoomAuditResponse> response = audits.stream()
                    .map(a -> RoomAuditResponse.builder()
                            .id(a.getId())
                            .roomId(room.getId())
                            .roomNumber(room.getRoomNumber())
                            .bookingId(a.getBooking() != null ? a.getBooking().getId() : null)
                            .operationType(a.getOperationType())
                            .amountPaid(a.getAmountPaid())
                            .notes(a.getNotes())
                            .createdAt(a.getCreatedAt())
                            .build())
                    .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                    .collect(Collectors.toList());

            return StandardResponse.success(response, "Room audits fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching room audits for roomId={}: ", roomId, e);
            return StandardResponse.error("Failed to fetch room audits", "ROOM_AUDIT_FETCH_ERROR", null, e.getMessage());
        }
    }
}
