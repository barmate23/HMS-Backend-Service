package com.schoolerp.staff.dto;

public record SubModuleResponse(
        Integer id,
        String subModuleCode,
        String subModuleName,
        String description
) {}