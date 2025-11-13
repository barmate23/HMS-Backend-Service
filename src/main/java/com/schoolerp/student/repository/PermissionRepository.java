package com.schoolerp.student.repository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Page<Permission> findByRoleId(Long roleId, Pageable pageable);
    void deleteByRoleId(Long roleId);
}