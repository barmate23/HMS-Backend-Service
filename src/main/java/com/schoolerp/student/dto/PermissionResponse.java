package com.schoolerp.student.dto;

public record PermissionResponse(
        Integer id,
        Integer moduleId,
        String moduleName,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete) {}