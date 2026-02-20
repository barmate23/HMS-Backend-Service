package com.schoolerp.staff.dto;

import com.schoolerp.staff.constants.StaffStatus;

import java.time.LocalDate;

public record StaffResponse(
                Integer id,
                String staffCode,
                String firstName,
                String lastName,
                String email,
                String phone,
                LocalDate dob,
                String fatherName,
                StaffStatus status,
                Integer departmentId,
                String departmentName,
                Integer designationId,
                String designationName,
                String licenseNumber,
                byte[] staffImage) {
}
