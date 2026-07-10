package com.hotelerp.frontoffice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accompanying_guests", indexes = {
        @Index(name = "idx_acc_reservation_id", columnList = "reservation_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccompanyingGuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // ── Personal Info ─────────────────────────────────────────────────────

    @Column(name = "title", length = 10)
    private String title;           // e.g. Mr., Mrs., Ms.

    @Column(name = "fullName", nullable = false, length = 150)
    private String fullName;

    @Column(name = "gender", length = 10)
    private String gender;          // MALE, FEMALE, OTHER

    @Column(name = "dateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "relationship", length = 100)
    private String relationship;    // e.g. Spouse, Child, Parent

    // ── Identity Proof ────────────────────────────────────────────────────

    @Column(name = "idProofType", length = 50)
    private String idProofType;     // e.g. Aadhar Card, Passport, etc.

    @Column(name = "idNumber", length = 100)
    private String idNumber;

    // ── Audit ─────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
