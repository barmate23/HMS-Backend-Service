package com.hotelerp.frontoffice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_audits", indexes = {
    @Index(name = "idx_ra_room_id", columnList = "roomId"),
    @Index(name = "idx_ra_booking_id", columnList = "bookingId"),
    @Index(name = "idx_ra_op_type", columnList = "operationType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomId", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingId")
    private Booking booking;

    @Column(name = "operationType", nullable = false, length = 50)
    private String operationType; // RESERVATION, CHECK_IN, CHECK_OUT, CANCEL

    @Builder.Default
    @Column(name = "amountPaid", precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Builder.Default
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
