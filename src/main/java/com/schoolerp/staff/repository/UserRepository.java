package com.schoolerp.staff.repository;


import com.schoolerp.staff.constants.StaffStatus;
import com.schoolerp.staff.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);
    UserEntity findById(Integer id);

    boolean existsByStaffCode(String staffCode);

    @Query("""
        SELECT s FROM UserEntity s
        WHERE s.isDeleted = false
          AND (:deptId IS NULL OR s.department.id = :deptId)
          AND (:status IS NULL OR s.status = :status)
          AND (:q IS NULL OR LOWER(CONCAT(s.firstName,' ',s.lastName,s.email)) LIKE LOWER(CONCAT('%',:q,'%')))
        """)
    Page<UserEntity> search(@Param("deptId") Long deptId,
                            @Param("status") StaffStatus status,
                            @Param("q") String q,
                            Pageable pageable);

    List<UserEntity> findByIsDeletedAndDesignationNameAndStatus(boolean b, String teacher, StaffStatus active);
}