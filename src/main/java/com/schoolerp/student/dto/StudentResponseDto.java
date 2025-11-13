package com.schoolerp.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class StudentResponseDto {
    
    private Integer id;
    
    private String admissionNo;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admissionDate;
    
    private String firstName;
    
    private String middleName;
    
    private String lastName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String bloodGroup;
    
    private String aadharNumber;
    
    private String caste;
    
    private String religion;
    
    private String nationality;
    
    private String motherTongue;
    
    private String currentAddress;
    
    private String permanentAddress;
    
    private String city;
    
    private String state;
    
    private String pincode;
    
    private String mobileNumber;
    
    private String alternateContact;
    
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
    
    private String mediumOfInstruction;
    
    private Integer status;
    
    private String feesStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;

    List<StudentDocumentDto> studentDocuments;;

    private FeeStructureResponse feeStructureResponse;
}