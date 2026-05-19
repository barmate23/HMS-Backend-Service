package com.hotelerp.frontoffice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guests", indexes = {
    @Index(name = "idx_guest_email", columnList = "email"),
    @Index(name = "idx_guest_phone", columnList = "phone"),
    @Index(name = "idx_guest_first_name", columnList = "firstName"),
    @Index(name = "idx_guest_is_deleted", columnList = "isDeleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // --- Personal Info ---
    @Enumerated(EnumType.STRING)
    @Column(name = "title", length = 10)
    private Title title;

    @Column(name = "firstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 50)
    private String lastName;

    @Column(name = "countryCode", length = 10)
    private String countryCode;          // e.g. +91

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // --- Address ---
    @Column(name = "addressLine1", length = 255)
    private String addressLine1;

    @Column(name = "addressLine2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postCode", length = 20)
    private String postCode;

    @Column(name = "country", length = 100)
    private String country;

    // --- Identity & Additional Info ---
    @Column(name = "nationality", length = 50)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "dateOfBirth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "idProofType")
    private IdProofType idProofType;

    @Column(name = "idProofNumber", length = 50)
    private String idProofNumber;

    @Column(name = "guestNotes", columnDefinition = "TEXT")
    private String guestNotes;

    @Column(name = "preference", columnDefinition = "TEXT")
    private String preference;

    @Builder.Default
    @Column(name = "isVip")
    private Boolean isVip = false;

    // --- Audit ---
    @Builder.Default
    @Column(name = "isActive")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Enums ────────────────────────────────────────────────────────────────

    public enum Title {
        MR, MRS, MS, MISS, DR, PROF
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum IdProofType {
        PASSPORT, AADHAR, DRIVING_LICENSE, PAN, VOTER_ID
    }
}