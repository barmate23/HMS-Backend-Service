package com.schoolerp.staff.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentDocumentRequestList {
    private List<StudentDocumentRequest> requests;
}
