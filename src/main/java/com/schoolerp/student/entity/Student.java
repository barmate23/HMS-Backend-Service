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
import java.util.Map;

@Entity
@Table(name = "students")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(unique = true)
    private String admissionNo;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admissionDate;
    
    @Column(nullable = false)
    private String firstName;
    
    private String middleName;
    
    @Column(nullable = false)
    private String lastName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String bloodGroup;
    
    private String aadharNumber;
    
    private String caste;
    
    private String religion;
    
    @Builder.Default
    private String nationality = "Indian";
    
    private String motherTongue;
    
    @Column(length = 500)
    private String currentAddress;
    
    @Column(length = 500)
    private String permanentAddress;
    
    private String city;
    
    private String state;
    
    private String pincode;
    
    @Column(nullable = false)
    private String mobileNumber;
    
    private String alternateContact;
    
    private String emailId;
    
    private String fatherName;
    
    private String motherName;
    
    private String guardianName;
    
    private String fatherOccupation;
    
    private String motherOccupation;
    
    private Long annualIncome;
    
    private Integer classApplyingFor;
    
    private Integer section;

    private Integer academicYear;
    
    private String previousSchoolName;
    
    private String transferCertificateNo;
    
    private Integer previousClassPassed;
    
    private String board;
    
    @Builder.Default
    private String mediumOfInstruction = "English";
    
//    @ElementCollection
//    @CollectionTable(name = "student_documents", joinColumns = @JoinColumn(name = "student_id"))
//    @MapKeyColumn(name = "document_type")
//    @Column(name = "document_path")
//    private Map<String, String> documents;
    

    private Integer status ;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FeesStatus feesStatus = FeesStatus.UNPAID;
    
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private boolean isDeleted = false;
    
    public enum FeesStatus {
        PAID, UNPAID, PARTIAL, OVERDUE
    }
}