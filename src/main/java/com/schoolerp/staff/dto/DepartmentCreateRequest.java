package com.schoolerp.staff.dto;

import jakarta.validation.constraints.*;

public record DepartmentCreateRequest(
        @NotBlank String name,
        @NotBlank @Size(max = 10) String code,
        String description,
        Long hodId
) {}
