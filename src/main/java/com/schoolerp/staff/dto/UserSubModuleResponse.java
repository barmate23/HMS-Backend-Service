package com.schoolerp.staff.dto;

import java.util.List;

public record UserSubModuleResponse(
        Integer id,

        String role,
        String designation,
        String subModuleCode,
        String subModuleName,
        PermissionUserResponse permission
) {}