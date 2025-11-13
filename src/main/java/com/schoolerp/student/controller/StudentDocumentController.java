package com.schoolerp.student.controller;


import com.schoolerp.student.dto.StudentDocumentRequest;
import com.schoolerp.student.dto.StudentDocumentRequestList;
import com.schoolerp.student.entity.StudentDocument;
import com.schoolerp.student.service.StudentDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-service/")
@RequiredArgsConstructor
public class StudentDocumentController {

    private final StudentDocumentService documentService;

    @PostMapping(value = "/uploadDocuments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<StudentDocument>> uploadDocuments(
            @ModelAttribute StudentDocumentRequestList requestList
    ) throws Exception {
        List<StudentDocument> savedDocs = documentService.uploadStudentDocuments(requestList.getRequests());
        return ResponseEntity.ok(savedDocs);
    }


        @DeleteMapping("delete/{id}")
        public ResponseEntity<String> deleteDocument(@PathVariable Integer id) {
            documentService.deleteStudentDocument(id);
            return ResponseEntity.ok("Document deleted successfully");
        }


}
