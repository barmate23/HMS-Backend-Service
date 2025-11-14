package com.schoolerp.student.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String keyName;   // e.g. STAFF_DIRECTORY

    @Column(nullable = false)
    private String name;      // Human readable, e.g. "Staff Directory"

    @Column(length = 1000)
    private String description;
}
