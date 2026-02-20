package com.schoolerp.staff.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "staff_qualifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String qualification;

    private String specialization;

    private String university;

    private Integer passingYear;

    private String grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;
}
