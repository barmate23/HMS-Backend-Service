package com.hotelerp.frontoffice.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * Payload for a single accompanying member sent from the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccompanyingGuestRequest {

    private String title;           // Mr., Mrs., Ms., Dr., etc.
    private String fullName;
    private String gender;          // MALE | FEMALE | OTHER
    private LocalDate dateOfBirth;
    private String relationship;    // Spouse, Child, Parent, Friend, etc.
    private String idProofType;     // Aadhar Card, Passport, Driving License, etc.
    private String idNumber;
}
