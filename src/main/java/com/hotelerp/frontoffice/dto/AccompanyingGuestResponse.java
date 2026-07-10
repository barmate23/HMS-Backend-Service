package com.hotelerp.frontoffice.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * Accompanying member detail returned in reservation responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccompanyingGuestResponse {

    private Long id;
    private String title;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String relationship;
    private String idProofType;
    private String idNumber;
}
