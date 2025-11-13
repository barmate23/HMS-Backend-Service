package com.schoolerp.student.repository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    boolean existsByKeyNameIgnoreCase(String keyName);
}