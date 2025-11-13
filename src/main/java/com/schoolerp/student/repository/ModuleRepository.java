package com.schoolerp.student.repository;

import com.schoolerp.student.entity.StudentModule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<StudentModule, Long> {
    boolean existsByKeyNameIgnoreCase(String keyName);
}