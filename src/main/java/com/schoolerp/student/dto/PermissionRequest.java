package com.schoolerp.student.dto;

public record PermissionRequest(
        Integer subModuleId,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete) {}


