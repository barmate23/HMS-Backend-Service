package com.schoolerp.student.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fee_structure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Instead of storing text, reference to master table (class, section)
    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private CommonMaster classId;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private CommonMaster sectionId;

    @ManyToOne
    @JoinColumn(name = "academic_year_id", nullable = false)
    private CommonMaster academicYear;   // e.g. "2024-25"

    @Column(name = "fee_structure_name", nullable = false)
    private String feeStructureName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_frequency_id", nullable = false)
    private CommonMaster paymentFrequency;

    private BigDecimal tuitionFee;
    private BigDecimal admissionFee;
    private BigDecimal transportFee;
    private BigDecimal libraryFee;
    private BigDecimal examFee;
    private BigDecimal sportsFee;
    private BigDecimal labFee;
    private BigDecimal developmentFee;

    @Column(name = "total_fee")
    private BigDecimal totalFee;

    // --- Advanced Settings ---
    @Column(name = "max_discount")
    private BigDecimal maxDiscount;

    @Column(name = "late_fee_penalty")
    private BigDecimal lateFeePenalty;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "due_date")
    private LocalDate dueDate;

    private Boolean isDeleted;

    // Audit columns (optional, industry standard)
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDate updatedAt;
}
