package com.schoolerp.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StudentRequestDto {
    
    private String admissionNo;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admissionDate;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    private String middleName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String bloodGroup;
    
    private String aadharNumber;
    
    private String caste;
    
    private String religion;
    
    private String nationality = "Indian";
    
    private String motherTongue;
    
    private String currentAddress;
    
    private String permanentAddress;
    
    private String city;
    
    private String state;
    
    private String pincode;
    
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;
    
    private String alternateContact;
    
    @Email(message = "Valid email is required")
    private String emailId;
    
    private String fatherName;
    
    private String motherName;
    
    private String guardianName;
    
    private String fatherOccupation;
    
    private String motherOccupation;
    
    private Long annualIncome;
    
    private Integer classApplyingFor;
    
    private Integer section;
    
    private Integer academicYear;
    
    private String previousSchoolName;
    
    private String transferCertificateNo;
    
    private Integer previousClassPassed;
    
    private String board;
    
    private String mediumOfInstruction = "English";
    
    private Integer status;
    
    // Helper field for form handling
    private Boolean sameAsCurrentAddress = false;
}