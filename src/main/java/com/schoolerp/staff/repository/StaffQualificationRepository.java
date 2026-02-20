package com.schoolerp.staff.repository;

import com.schoolerp.staff.entity.Staff;
import com.schoolerp.staff.entity.StaffQualification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffQualificationRepository extends JpaRepository<StaffQualification, Long> {
    List<StaffQualification> findByStaff(Staff staff);
}
