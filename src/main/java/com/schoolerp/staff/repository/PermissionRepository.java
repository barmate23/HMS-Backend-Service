package com.schoolerp.staff.repository;

import com.schoolerp.staff.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Page<Permission> findByRoleId(Long roleId, Pageable pageable);
    void deleteByRoleId(Long roleId);
}