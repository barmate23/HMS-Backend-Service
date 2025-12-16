package com.schoolerp.staff.dto;

import jakarta.validation.constraints.NotBlank;

public record ModuleCreateRequest(
        @NotBlank String keyName,
        @NotBlank String name,
        String description) {}
