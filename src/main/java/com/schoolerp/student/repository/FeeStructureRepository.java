package com.schoolerp.student.repository;



import com.schoolerp.student.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long>, JpaSpecificationExecutor<FeeStructure> {
    FeeStructure findByIdAndIsDeletedFalse(Long id);

    Optional<FeeStructure> findByClassId_IdAndAcademicYear_IdAndIsDeletedFalse(Integer classApplyingFor, Integer academicYear);
}
