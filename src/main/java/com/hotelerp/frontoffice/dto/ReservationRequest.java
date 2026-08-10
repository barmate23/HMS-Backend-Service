package com.hotelerp.frontoffice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request payload to create a new Reservation + its room Bookings in one shot.
 *
 * Guest resolution (mutually exclusive — exactly one must be provided):
 *  - guestId      → "Search Guest" flow: use an existing saved guest.
 *  - guestDetails → "Create Guest" flow: create a new guest inline, then use it.
 *
 * Rooms:
 *  - roomIds list  → one Booking row is created per selected room.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    // ── Guest (provide ONE of the two below) ──────────────────────────────

    /**
     * ID of an existing guest.
     * Used when the front-end chose "Search Guest".
     */
    private Long guestId;

    /**
     * Full guest details to create a new guest inline.
     * Used when the front-end chose "Create Guest".
     * If both guestId and guestDetails are provided, guestId takes precedence.
     */
    @Valid
    private GuestRequest guestDetails;

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    // ── Stay Info ──────────────────────────────────────────────────────────

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    private LocalTime checkInTime;          // defaults to 14:00 server-side

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    private LocalTime checkOutTime;         // defaults to 11:00 server-side

    @NotNull(message = "Number of adults is required")
    @Positive(message = "Number of adults must be positive")
    private Integer numberOfAdults;
    private Integer gstPercent;

    private Integer numberOfChildren = 0;

//    @NotNull(message = "Reservation status is required")
    private Long reservationStatusId;

    // ── Room & Rate Plan ───────────────────────────────────────────────────

    /**
     * IDs of the rooms the guest has selected.
     * At least one room must be chosen.
     */
    @NotNull(message = "At least one room must be selected")
    private List<Long> roomIds;

    @NotNull(message = "Rate plan is required")
    private Long ratePlanId;

    // ── Billing Profile ────────────────────────────────────────────────────

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

    // ── Address Profile ───────────────────────────────────────────────────
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postCode;

    // ── Notes ─────────────────────────────────────────────────────────────

    private String specialRequests;
    private String notes;

    // ── Accompanying Members (Optional) ───────────────────────────────────

    /**
     * List of additional guests accompanying the primary guest.
     * Optional — can be null or empty.
     */
    @Valid
    private List<AccompanyingGuestRequest> accompanyingGuests;
}
