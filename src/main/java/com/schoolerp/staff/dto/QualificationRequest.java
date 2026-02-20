package com.schoolerp.staff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QualificationRequest(

        @NotBlank(message = "Qualification is required")
        String qualification,

        String specialization,

        String university,

        @NotNull(message = "Passing year is required")
        Integer passingYear,

        String grade

) {}
