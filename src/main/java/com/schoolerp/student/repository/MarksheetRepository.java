package com.schoolerp.student.repository;

import com.schoolerp.student.entity.Marksheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarksheetRepository extends JpaRepository<Marksheet, Integer> {
    
    List<Marksheet> findByStudentId(String studentId);
    
    List<Marksheet> findByStudentIdAndAcademicYear(String studentId, String academicYear);
    
    List<Marksheet> findByStatus(Marksheet.MarksheetStatus status);
    
    List<Marksheet> findByClassNameAndAcademicYear(String className, String academicYear);
}