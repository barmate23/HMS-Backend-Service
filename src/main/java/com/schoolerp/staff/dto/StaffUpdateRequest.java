package com.schoolerp.staff.dto;

import com.schoolerp.staff.constants.StaffStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record StaffUpdateRequest(
                /* ===== BASIC DETAILS ===== */
                @NotBlank(message = "First name is required") String firstName,

                @NotBlank(message = "Last name is required") String lastName,

                @Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email,

                String phone,

                LocalDate dob,

                Long departmentId,

                Long designationId,

                String fatherName,
                String licenseNumber,

                StaffStatus status,

                byte[] staffImage,

                /* ===== BANK DETAILS ===== */

                String bankName,

                String accountHolderName,

                String accountNumber,

                String ifscCode,

                String branchName,

                String upiId,

                /* ===== ADDRESS DETAILS ===== */

                String addressLine1,

                String addressLine2,

                String city,

                String state,

                String country,

                String postalCode,

                Integer roleId,
                /* ===== QUALIFICATIONS ===== */

                List<QualificationRequest> qualifications) {
}
