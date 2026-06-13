package com.hotelerp.frontoffice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A Booking represents a single-room assignment under a Reservation.
 * One Reservation can have many Bookings (one per room selected).
 */
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bk_reservation_id", columnList = "reservationId"),
        @Index(name = "idx_bk_room_id",        columnList = "roomId"),
        @Index(name = "idx_bk_checkin_date",   columnList = "checkInDate"),
        @Index(name = "idx_bk_status",         columnList = "bookingStatus"),
        @Index(name = "idx_bk_is_deleted",     columnList = "isDeleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ── Relations ─────────────────────────────────────────────────────────

    /** Parent reservation */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservationId", nullable = false)
    private Reservation reservation;

    /** The specific room assigned in this booking line */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomId", nullable = false)
    private Room room;

    // ── Dates (copied from reservation for quick queries) ─────────────────

    @Column(name = "checkInDate", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "checkOutDate", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "numberOfNights", nullable = false)
    private Integer numberOfNights;

    // ── Pricing ───────────────────────────────────────────────────────────

    /** Base room rate (per night) at time of booking */
    @Column(name = "ratePerNight", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerNight;

    /** Rate-plan add-on per night (extracted from RatePlan entity) */
    @Builder.Default
    @Column(name = "ratePlanCharge", precision = 10, scale = 2)
    private BigDecimal ratePlanCharge = BigDecimal.ZERO;

    /** ratePerNight + ratePlanCharge  × numberOfNights */
    @Column(name = "totalPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Builder.Default
    @Column(name = "discountPercentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discountAmount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** totalPrice − discountAmount */
    @Column(name = "finalPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    // ── Status ────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_status_id")
    private CommonMaster bookingStatus;

    // ── Audit ─────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

}
