package com.schoolerp.student.repository;

import com.schoolerp.student.entity.RoleStaffMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleStaffMapperRepository extends JpaRepository<RoleStaffMapper, Long> {
    List<RoleStaffMapper> findByIsDeletedAndRoleId(boolean isDeleted, Integer roleId);
}


