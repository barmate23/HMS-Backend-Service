package com.hotelerp.frontoffice.dto;

import com.hotelerp.common.entity.Guest;
import com.hotelerp.common.entity.Guest.Title;
import com.hotelerp.common.entity.Guest.Gender;
import com.hotelerp.common.entity.Guest.IdProofType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * Request payload for creating / updating a Guest.
 * Maps 1-to-1 with the "Guest Information" and "Identity & Additional Info"
 * sections of the New Booking screen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestRequest {

    // ── Personal Info ─────────────────────────────────────────────────────

    private Title title;                  // MR, MRS, MS, MISS, DR, PROF

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String countryCode;                 // +91, +1 …

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    private String email;

    // ── Address ───────────────────────────────────────────────────────────

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postCode;
    private String country;

    // ── Identity & Additional Info ────────────────────────────────────────

    private String nationality;
    private Gender gender;
    private LocalDate dateOfBirth;

    private IdProofType idProofType;
    private String idProofNumber;

    private String guestNotes;
    private String preference;

    @Builder.Default
    private Boolean isVip = false;
}
