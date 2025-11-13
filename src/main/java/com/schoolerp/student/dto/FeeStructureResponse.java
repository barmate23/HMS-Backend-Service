package com.schoolerp.student.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FeeStructureResponse {

    private Long id;
    private Long classId;
    private String className;

    private Long sectionId;
    private String sectionName;

    private Long academicYearId;
    private String academicYearName;

    private String feeStructureName;

    private Long paymentFrequencyId;
    private String paymentFrequencyName;

    private BigDecimal tuitionFee;
    private BigDecimal admissionFee;
    private BigDecimal transportFee;
    private BigDecimal libraryFee;
    private BigDecimal examFee;
    private BigDecimal sportsFee;
    private BigDecimal labFee;
    private BigDecimal developmentFee;

    private BigDecimal totalFee;

    private BigDecimal maxDiscount;
    private BigDecimal lateFeePenalty;

    private LocalDate effectiveFrom;
    private LocalDate dueDate;

    private Boolean isDeleted;

    private String createdBy;
    private LocalDate createdAt;

    private String updatedBy;
    private LocalDate updatedAt;
}
