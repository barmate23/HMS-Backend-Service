package com.schoolerp.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class IdCardRequestDto {
    
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Card number is required")
    private String cardNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validFrom;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validTo;
    
    private String status = "active";
    
    private String photoUrl;
    
    private String qrCode;
    
    private String issueReason = "New Admission";
}