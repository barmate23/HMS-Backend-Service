package com.schoolerp.staff.dto;

import com.schoolerp.staff.constants.StaffStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record StaffCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String phone,
        LocalDate dob,
        Long departmentId,
        Long designationId,
        String fatherName,
        StaffStatus status,
        String username,
        byte[] staffImage
) {
}
