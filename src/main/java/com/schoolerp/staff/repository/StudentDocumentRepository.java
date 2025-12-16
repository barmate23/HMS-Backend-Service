package com.schoolerp.staff.repository;

import com.schoolerp.staff.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Integer> {

    List<StudentDocument> findByStudentIdAndIsDeletedFalse(Integer id);

    StudentDocument findByIdAndIsDeletedFalse(Integer documentId);
}