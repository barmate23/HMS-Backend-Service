package com.schoolerp.student.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code; // e.g. ACAD, ACC

    // Will be replaced later by @ManyToOne Staff
    private Long hodId;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private Boolean isDelete;
}
