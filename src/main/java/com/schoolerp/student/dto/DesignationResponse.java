package com.schoolerp.student.dto;

public record DesignationResponse(
        Integer id,
        String name,
        String description,
        boolean teaching,
        Integer departmentId,
        String departmentName,
        String hodName,
        String hodDesignation,
        String hodImgUrl
) {
}
