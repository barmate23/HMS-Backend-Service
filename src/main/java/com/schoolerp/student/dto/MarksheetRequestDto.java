package com.schoolerp.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class MarksheetRequestDto {
    
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    private String examType = "Annual Exam";
    
    private String academicYear = "2024-25";
    
    private String className;
    
    private String section;
    
    private String subjects;
    
    private Integer totalMarks = 0;
    
    private Integer maxTotalMarks = 500;
    
    private Double percentage = 0.0;
    
    private String grade;
    
    private Integer rank = 0;
    
    private String result = "Pass";
    
    private String status = "draft";
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
}