package com.schoolerp.student.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleCreateRequest(@NotBlank String name, String description) {}
public record RoleUpdateRequest(@NotBlank String name, String description) {}
public record RoleResponse(Long id, String name, String description) {}
