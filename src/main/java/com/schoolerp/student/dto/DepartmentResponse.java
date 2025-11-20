package com.schoolerp.student.dto;

public record DepartmentResponse(
        Integer id,
        String name,
        String code,
        String description,
        Long hodId,
        Integer designationsCount,
        String hodName,
        String hodDesignation,
        String hodImgUrl
) {}
