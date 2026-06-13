package com.hotelerp.frontoffice.dto;

import com.hotelerp.common.entity.Reservation;
import com.hotelerp.common.entity.Reservation.ReservationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Listing-only response for a Reservation.
 * Contains exactly what the 6 listing columns need — nothing more.
 *
 * Use GET /getReservationById/{id} for full detail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long id;

    // ── GUEST DETAILS column ───────────────────────────────────────────────
    private Long   guestId;
    private String guestInitials;   // e.g. "JD"
    private String guestFullName;   // e.g. "John Doe"
    private String guestPhone;      // e.g. "+1 555-0101"
    private String guestBadge;      // "VIP" | "REPEAT" | "NEW"

    // ── ROOM & PLAN column ─────────────────────────────────────────────────
    private Integer           numberOfRooms;
    private List<RoomSummary> rooms;           // one entry per booked room

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomSummary {
        private String roomNumber;      // e.g. "102"
        private String roomTypeName;    // e.g. "Double", "Suite"
        private String ratePlanName;    // e.g. "European Plan", "Breakfast Included"
    }

    // ── STAY PERIOD column ─────────────────────────────────────────────────
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer   numberOfNights;
    private Integer   numberOfAdults;
    private Integer   numberOfChildren;

    // ── STATUS column ──────────────────────────────────────────────────────
    private ReservationStatus reservationStatus;

    // ── BILLING column ─────────────────────────────────────────────────────
    private BigDecimal grandTotal;    // sum of Booking.finalPrice
    private BigDecimal paidAmount;    // sum of successful Payments
}
