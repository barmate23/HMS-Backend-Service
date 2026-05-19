package com.hotelerp.frontoffice.dto;

import com.hotelerp.frontoffice.entity.Booking;
import com.hotelerp.frontoffice.entity.Floor;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single room-booking line within a Reservation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long reservationId;

    // ── Room Info ──────────────────────────────────────────────────────────
    private Long roomId;
    private String roomNumber;
    private String roomTypeName;
    private String floor;
    private String viewType;            // e.g. "Sea View", "Garden View"
    private String bedType;             // e.g. "King", "Twin"

    // ── Dates ─────────────────────────────────────────────────────────────
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfNights;

    // ── Pricing ───────────────────────────────────────────────────────────
    private BigDecimal ratePerNight;
    private BigDecimal ratePlanCharge;
    private BigDecimal totalPrice;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;

    // ── Status ────────────────────────────────────────────────────────────
    private Booking.BookingStatus bookingStatus;

    // ── Audit ─────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
