package com.schoolerp.student.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentDocumentsWrapper {
    private List<StudentDocumentRequest> documents;
}
