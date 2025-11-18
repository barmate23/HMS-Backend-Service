package com.schoolerp.student.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequest(@NotBlank String name, String code, String description) {}
