package com.schoolerp.student.repository;

import com.schoolerp.student.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Integer> {

    List<StudentDocument> findByStudentIdAndIsDeletedFalse(Integer id);

    StudentDocument findByIdAndIsDeletedFalse(Integer documentId);
}