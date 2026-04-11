package com.schoolerp.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffExcelDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dob; // can parse date
    private String fatherName;
    private String licenseNumber;
    private String departmentName;
    private String designationName;
    private String status;

    // Bank Details
    private String bankName;
    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;
    private String branchName;
    private String upiId;

    // Address Details
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
