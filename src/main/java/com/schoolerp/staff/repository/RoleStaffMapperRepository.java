package com.schoolerp.staff.repository;

import com.schoolerp.staff.entity.RoleStaffMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleStaffMapperRepository extends JpaRepository<RoleStaffMapper, Long> {
    List<RoleStaffMapper> findByIsDeletedAndRoleId(boolean isDeleted, Integer roleId);

    RoleStaffMapper findByIsDeletedAndStaffId(boolean b, int i);
}


