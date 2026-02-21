package com.schoolerp.staff.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RoleCreateRequest(@NotBlank String name, String code, String description, List<Integer> staffIds) {
}
