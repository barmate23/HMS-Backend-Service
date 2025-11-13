package com.schoolerp.student.service;

import com.schoolerp.student.dto.StudentDocumentRequest;
import com.schoolerp.student.entity.StudentDocument;
import com.schoolerp.student.repository.StudentDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StudentDocumentServiceImpl implements StudentDocumentService {

    @Autowired
    private final S3Client s3Client;
    @Autowired
    private final StudentDocumentRepository repository;

    private final String bucketName = "erp-school-management";

    public StudentDocumentServiceImpl(S3Client s3Client, StudentDocumentRepository repository) {
        this.s3Client = s3Client;
        this.repository = repository;
    }


        @Override
        public List<StudentDocument> uploadStudentDocuments(List<StudentDocumentRequest> requests) throws IOException {
            List<StudentDocument> result = new ArrayList<>();

            for (StudentDocumentRequest req : requests) {
                MultipartFile file = req.getFile();
                Integer studentId = req.getStudentId() != null ? req.getStudentId().intValue() : null;

                if (studentId == null) {
                    throw new IllegalArgumentException("studentId is required for the document");
                }
                if (file == null || file.isEmpty()) {
                    throw new IllegalArgumentException("File is required for each document upload");
                }

                String key = "students/" + studentId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

                try {
                    s3Client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(key)
                                    .contentType(file.getContentType())
                                    .build(),
                            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
                    );
                } catch (S3Exception e) {
                    throw new RuntimeException("S3 Upload failed: " + e.awsErrorDetails().errorMessage(), e);
                }

                String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + key;

                StudentDocument doc = new StudentDocument();
                doc.setStudentId(studentId);
                doc.setDocumentType(req.getDocumentType());
                doc.setS3Key(key);
                doc.setS3Url(s3Url);
                doc.setDocumentName(file.getOriginalFilename());

                result.add(repository.save(doc));
            }

            return result;
        }

    @Override
    public void deleteStudentDocument(Integer documentId) {
        StudentDocument doc = repository.findByIdAndIsDeletedFalse(documentId);
        if (doc == null) {
            throw new IllegalArgumentException("Document not found with id: " + documentId);
        }
        doc.setDeleted(true);
        repository.save(doc);
    }





}

