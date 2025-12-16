package com.schoolerp.staff.dto;


import com.schoolerp.staff.constants.StaffStatus;

import java.time.LocalDate;

public record StaffAllResponse(
        Integer id,
        String fullName
) {}
