package com.schoolerp.student.repository;

import com.schoolerp.staff.enums.StaffStatus;
import com.schoolerp.staff.model.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStaffCode(String staffCode);

    @Query("""
        SELECT s FROM Staff s
        WHERE s.deleted = false
          AND (:deptId IS NULL OR s.department.id = :deptId)
          AND (:status IS NULL OR s.status = :status)
          AND (:q IS NULL OR LOWER(CONCAT(s.firstName,' ',s.lastName,s.email)) LIKE LOWER(CONCAT('%',:q,'%')))
        """)
    Page<Staff> search(@Param("deptId") Long deptId,
                       @Param("status") StaffStatus status,
                       @Param("q") String q,
                       Pageable pageable);
}
