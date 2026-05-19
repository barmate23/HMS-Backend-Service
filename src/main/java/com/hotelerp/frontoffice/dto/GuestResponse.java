package com.hotelerp.frontoffice.dto;

import com.hotelerp.frontoffice.entity.Guest;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response payload returned after Guest CRUD operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponse {

    private Long id;

    // Personal
    private Guest.Title title;
    private String firstName;
    private String lastName;
    private String fullName;               // firstName + " " + lastName
    private String countryCode;
    private String phone;
    private String email;

    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postCode;
    private String country;

    // Identity
    private String nationality;
    private Guest.Gender gender;
    private LocalDate dateOfBirth;
    private Guest.IdProofType idProofType;
    private String idProofNumber;

    // Extras
    private String guestNotes;
    private String preference;
    private Boolean isVip;
    private Boolean isActive;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
