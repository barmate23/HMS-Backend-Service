package com.schoolerp.staff.dto;

import com.schoolerp.staff.constants.StaffStatus;

import java.time.LocalDate;
import java.util.List;

public record StaffDetailsResponse(
        Integer id,
        String staffCode,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dob,
        String fatherName,
        String licenseNumber,
        StaffStatus status,
        Integer departmentId,
        String departmentName,
        Integer designationId,
        String designationName,
        byte[] staffImage,

        // Bank Details
        String bankName,
        String accountHolderName,
        String accountNumber,
        String ifscCode,
        String branchName,
        String upiId,

        // Address Details
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,

        // Qualifications
        List<QualificationRequest> qualifications) {
}
