package com.schoolerp.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDocumentDto {
    private Integer id;
    private String documentType;
    private String documentName;
    private String s3Url;
}
