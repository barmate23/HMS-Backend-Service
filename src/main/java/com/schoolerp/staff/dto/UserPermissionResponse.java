package com.schoolerp.staff.dto;

import java.util.List;

public record UserPermissionResponse(Integer id, String module, String moduleCode,  List<UserSubModuleResponse> subModuleResponseList) {}
