package com.schoolerp.student.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "marksheets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Marksheet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(nullable = false)
    private String studentId;
    
    private String examType;
    
    private String academicYear;
    
    private String className;
    
    private String section;
    
    @Column(length = 1000)
    private String subjects;
    
    private Integer totalMarks;
    
    @Builder.Default
    private Integer maxTotalMarks = 500;
    
    private Double percentage;
    
    private String grade;

    @Column(name = "student_rank")
    private Integer rank;
    
    @Builder.Default
    private String result = "Pass";
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MarksheetStatus status = MarksheetStatus.DRAFT;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
    
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;
    
    public enum MarksheetStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}