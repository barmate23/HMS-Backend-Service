package com.schoolerp.student.dto;

public record SubModuleResponse(
        Integer id,
        String subModuleCode,
        String subModuleName,
        String description
) {}