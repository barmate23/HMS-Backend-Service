package com.schoolerp.student.dto;

import com.schoolerp.staff.enums.StaffStatus;
import java.time.LocalDate;

public record StaffResponse(
        Long id,
        String staffCode,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dob,
        String fatherName,
        StaffStatus status,
        Long departmentId,
        String departmentName,
        Long designationId,
        String designationName
) {}
