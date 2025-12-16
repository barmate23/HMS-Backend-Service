package com.schoolerp.staff.dto;

public record PermissionResponse(
        Integer id,
        Integer moduleId,
        String moduleName,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete) {}