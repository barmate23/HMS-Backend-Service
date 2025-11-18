package com.schoolerp.student.dto;

import java.util.List;

public record ModuleWithSubmodulesResponse(
        Integer id,
        String keyName,
        String name,
        String description,
        List<SubModuleResponse> subModules
) {}

