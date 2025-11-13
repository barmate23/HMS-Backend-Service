package com.schoolerp.student.dto;

public record DesignationResponse(
        Long id,
        String name,
        String description,
        boolean teaching,
        Long departmentId,
        String departmentName
) {}
