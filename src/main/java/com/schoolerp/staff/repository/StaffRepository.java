package com.schoolerp.staff.repository;


import com.schoolerp.staff.constants.StaffStatus;
import com.schoolerp.staff.entity.Designation;
import com.schoolerp.staff.entity.Staff;
import com.schoolerp.staff.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {

 @Query("""
        SELECT s FROM Staff s
        WHERE s.isDeleted = false
          AND (:deptId IS NULL OR s.department.id = :deptId)
          AND (:designationId IS NULL OR s.designation.name = :designationId)
          AND (:status IS NULL OR s.status = :status)
          AND (:q IS NULL OR LOWER(CONCAT(s.firstName,' ',s.lastName,s.email)) LIKE LOWER(CONCAT('%',:q,'%')))
        """)
 Page<Staff> search(@Param("deptId") Long deptId,
                         @Param("designationId") String designation, @Param("status") StaffStatus status,
                         @Param("q") String q,
                         Pageable pageable);

 List<Staff> findByIsDeletedAndDesignationNameAndStatus(boolean b, String teacher, StaffStatus active);

    boolean existsByStaffCode(String code);
}
