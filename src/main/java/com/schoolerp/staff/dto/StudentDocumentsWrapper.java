package com.schoolerp.staff.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentDocumentsWrapper {
    private List<StudentDocumentRequest> documents;
}
