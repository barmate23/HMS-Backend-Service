package com.schoolerp.staff.dto;

public record PermissionRequest(
        Integer subModuleId,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete) {}


