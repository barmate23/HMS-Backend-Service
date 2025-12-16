package com.schoolerp.staff.dto;

import java.util.List;

public record RoleResponse(Integer id, String name, String code, String description, List<StaffResponse> staffResponseList) {}
