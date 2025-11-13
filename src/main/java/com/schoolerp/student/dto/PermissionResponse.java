package com.schoolerp.student.dto;

public record PermissionResponse(
        Long id,
        Long moduleId,
        String moduleName,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete) {}