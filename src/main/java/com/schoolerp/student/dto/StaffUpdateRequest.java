package com.schoolerp.student.dto;


import com.schoolerp.student.constants.StaffStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record StaffUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String phone,
        LocalDate dob,
        Long departmentId,
        Long designationId,
        String fatherName,
        StaffStatus status,
        byte[] staffImage
) {}
