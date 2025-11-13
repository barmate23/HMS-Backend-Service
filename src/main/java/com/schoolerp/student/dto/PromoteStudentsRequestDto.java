package com.schoolerp.student.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromoteStudentsRequestDto {

    @NotNull(message = "From class is required")
    @Min(value = 1, message = "From class must be greater than 0")
    private Integer fromClass;

    @NotNull(message = "From section is required")
    @Min(value = 1, message = "From section must be greater than 0")
    private Integer fromSection;

    @NotNull(message = "To class is required")
    @Min(value = 1, message = "To class must be greater than 0")
    private Integer toClass;

    @NotNull(message = "To section is required")
    @Min(value = 1, message = "To section must be greater than 0")
    private Integer toSection;

    @NotNull(message = "Academic year is required")
    private Integer academicYear;

    private boolean markAsAlumni = false;

    private boolean sendNotification = true;

    private String status;

    @NotEmpty(message = "At least one student ID is required")
    private List<Integer> studentIds;
}
