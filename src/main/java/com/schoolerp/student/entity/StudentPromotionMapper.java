package com.schoolerp.student.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_promotion_mapper")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentPromotionMapper {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Integer fromClass;

    @Column(nullable = false)
    private Integer fromSection;

    @Column(nullable = false)
    private Integer toClass;

    @Column(nullable = false)
    private Integer toSection;

    @Column(nullable = false)
    private Integer academicYear;

    @Column(nullable = false)
    private LocalDateTime promotionDate;

    private Integer status;

}

