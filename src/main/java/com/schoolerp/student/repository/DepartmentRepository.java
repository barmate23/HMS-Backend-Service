package com.schoolerp.student.repository;

import com.schoolerp.student.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT d FROM Department d
            WHERE d.deleted = false 
            AND (:q IS NULL 
                OR LOWER(d.name) LIKE LOWER(CONCAT('%',:q,'%'))
                OR LOWER(d.code) LIKE LOWER(CONCAT('%',:q,'%')))
           """)
    Page<Department> search(String q, Pageable pageable);
}
