package com.hotelerp.frontoffice.dto;

import com.hotelerp.frontoffice.entity.Reservation;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Full detail response returned by GET /getReservationById/{id}.
 * Contains every field needed to render the detail / edit view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailResponse {

    private Long id;

    /** System-generated booking confirmation number (e.g. BK-2026-1108-0042), shared across all rooms */
    private String confirmationNumber;

    // ── Guest ──────────────────────────────────────────────────────────────
    private Long guestId;
    private String guestInitials;
    private String guestFullName;
    private String guestEmail;
    private String guestPhone;
    private String guestTitle;
    private String guestFirstName;
    private String guestLastName;
    private String guestCountryCode;
    private String guestAddressLine1;
    private String guestAddressLine2;
    private String guestCity;
    private String guestState;
    private String guestPostCode;
    private String guestCountry;
    private String guestNationality;
    private String guestGender;
    private LocalDate guestDateOfBirth;
    private String guestIdProofType;
    private String guestIdProofNumber;
    private String guestNotes;
    private Boolean guestIsVip;
    private String guestBadge; // "VIP" | "REPEAT" | "NEW"

    // ── Hotel ──────────────────────────────────────────────────────────────
    private Long hotelId;
    private String hotelName;

    // ── Stay Info ──────────────────────────────────────────────────────────
    private LocalDate checkInDate;
    private LocalTime checkInTime;
    private LocalDate checkOutDate;
    private LocalTime checkOutTime;
    private Integer numberOfNights;
    private Integer numberOfAdults;
    private Integer numberOfChildren;

    // ── Status & Plan ──────────────────────────────────────────────────────
    private String reservationStatus;
    private Long ratePlanId;
    private String ratePlanName;

    // ── Rooms & Booking Lines ──────────────────────────────────────────────
    private Integer numberOfRooms;
    private List<BookingResponse> bookings; // full per-room detail

    // ── Billing ────────────────────────────────────────────────────────────
    private String billingName;
    private String billingAddress;
    private String billingMode;
    private String gstNumber;
    private String organisationName;
    private String travelAgentName;
    private String businessSource;
    private String marketSegment;
    private String bookingReference;
    private String bookingFrom;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postCode;
    private BigDecimal totalPrice; // before discount
    private BigDecimal totalDiscount;
    private BigDecimal grandTotal; // after discount
    private Integer gstPercent;
    private BigDecimal paidAmount; // from successful payments

    // ── Notes ─────────────────────────────────────────────────────────────
    private String specialRequests;
    private String notes;

    // ── Accompanying Members ───────────────────────────────────────────────
    private List<AccompanyingGuestResponse> accompanyingGuests;

    // ── Audit ─────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
