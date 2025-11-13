package com.schoolerp.student.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentDocumentRequestList {
    private List<StudentDocumentRequest> requests;
}
