package com.schoolerp.student.dto;

import jakarta.validation.constraints.NotBlank;

public record ModuleUpdateRequest(
        @NotBlank String keyName,
        @NotBlank String name,
        String description) {}

