package com.schoolerp.student.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RoleUpdateRequest(@NotBlank String name, String code, String description, List<Integer> StaffIds) {}
