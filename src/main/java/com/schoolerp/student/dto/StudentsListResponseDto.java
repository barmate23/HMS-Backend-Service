package com.schoolerp.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentsListResponseDto {
    
    private List<StudentResponseDto> students;
    
    private long total;
}