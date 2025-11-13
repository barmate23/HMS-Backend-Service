package com.schoolerp.student.dto;

public record ModuleUpdateRequest(
        @NotBlank String keyName,
        @NotBlank String name,
        String description) {}

public record ModuleResponse(Long id, String keyName, String name, String description) {}
