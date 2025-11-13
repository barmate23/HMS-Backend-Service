package com.schoolerp.student.dto;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        String description,
        Long hodId
) {}
