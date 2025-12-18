package com.schoolerp.staff.dto;

public record PermissionUserResponse(
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete) {}