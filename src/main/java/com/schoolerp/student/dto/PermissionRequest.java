package com.schoolerp.student.dto;

public record PermissionRequest(
        Long moduleId,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete) {}

public record PermissionResponse(
        Long id,
        Long moduleId,
        String moduleName,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete) {}
