package com.schoolerp.student.repository;

import com.schoolerp.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

    
    boolean existsByAdmissionNo(String admissionNo);

    Optional<Student> findByIdAndIsDeletedFalse(Integer id);

    @Query("SELECT s FROM Student s WHERE " +
           "(:search IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.admissionNo) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:classApplyingFor IS NULL OR s.classApplyingFor = :classApplyingFor) AND " +
           "s.isDeleted = false")
    Page<Student> findStudentsWithFilters(
            @Param("search") String search,
            @Param("status") Integer status,
            @Param("classApplyingFor") Integer classApplyingFor,
            Pageable pageable);
    
    @Query("SELECT COUNT(s) FROM Student s WHERE " +
           "(:search IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.admissionNo) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:classApplyingFor IS NULL OR s.classApplyingFor = :classApplyingFor) AND " +
           "s.isDeleted = false")
    long countStudentsWithFilters(
            @Param("search") String search,
            @Param("status") String status,
            @Param("classApplyingFor") Integer classApplyingFor);
    
    @Query("SELECT s FROM Student s WHERE s.classApplyingFor = :fromClass AND s.section = :fromSection AND s.isDeleted = false")
    List<Student> findByClassAndSection(
            @Param("fromClass") Integer fromClass,
            @Param("fromSection") Integer fromSection);

    List<Student> findAllByIdInAndIsDeletedFalse(List<Integer> ids);

}