package com.hotelerp.frontoffice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_res_guest_id", columnList = "guestId"),
        @Index(name = "idx_res_checkin_date", columnList = "checkInDate"),
        @Index(name = "idx_res_status", columnList = "reservationStatus"),
        @Index(name = "idx_res_is_deleted", columnList = "isDeleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ── Relations ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guestId", nullable = false)
    private Guest guest;

    // ── Stay Info ─────────────────────────────────────────────────────────

    @Column(name = "checkInDate", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "checkInTime")
    private LocalTime checkInTime; // default 14:00

    @Column(name = "checkOutDate", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "checkOutTime")
    private LocalTime checkOutTime; // default 11:00

    @Column(name = "numberOfNights", nullable = false)
    private Integer numberOfNights;

    @Column(name = "numberOfAdults", nullable = false)
    private Integer numberOfAdults;

    @Column(name = "numberOfChildren")
    private Integer numberOfChildren = 0;

    /** Convenience: adults + children */
    public Integer getTotalGuests() {
        return (numberOfAdults != null ? numberOfAdults : 0)
                + (numberOfChildren != null ? numberOfChildren : 0);
    }

    @Column(name = "numberOfRooms", nullable = false)
    private Integer numberOfRooms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_status_id")
    private CommonMaster reservationStatus;

    // ── Rate Plan ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ratePlanId", nullable = false)
    private RatePlan ratePlan;

    // ── Billing ───────────────────────────────────────────────────────────

    @Column(name = "billingName", length = 255)
    private String billingName;

    @Column(name = "billingAddress", columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "billingMode", length = 50)
    private String billingMode;

    @Column(name = "gstNumber", length = 50)
    private String gstNumber;

    @Column(name = "organisationName", length = 100)
    private String organisationName;

    @Column(name = "travelAgentName", length = 100)
    private String travelAgentName;

    @Column(name = "businessSource", length = 50)
    private String businessSource;

    @Column(name = "marketSegment", length = 50)
    private String marketSegment;

    @Column(name = "bookingReference", length = 100)
    private String bookingReference;

    // ── Notes ─────────────────────────────────────────────────────────────

    @Column(name = "specialRequests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
