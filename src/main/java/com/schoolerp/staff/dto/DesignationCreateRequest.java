package com.schoolerp.staff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DesignationCreateRequest(
        @NotBlank String name,
        String description,
        boolean teaching,
        @NotNull Long departmentId
) {}
