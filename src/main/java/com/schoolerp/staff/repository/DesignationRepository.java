package com.schoolerp.staff.repository;


import com.schoolerp.staff.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignationRepository extends JpaRepository<Designation, Long> {

    Page<Designation> findByDepartmentIdAndIsDeletedFalse(Long departmentId, Pageable pageable);
    List<Designation> findByDepartmentIdAndIsDeleted(Integer departmentId, Boolean isDeleted);
}
