package com.schoolerp.student.service;

import com.schoolerp.student.dto.StudentDocumentRequest;
import com.schoolerp.student.entity.StudentDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StudentDocumentService {
    List<StudentDocument> uploadStudentDocuments(List<StudentDocumentRequest> requests) throws IOException;

    void deleteStudentDocument(Integer documentId);
}
