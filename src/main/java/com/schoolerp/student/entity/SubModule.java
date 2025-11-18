package com.schoolerp.student.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subModules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Modules modules;

    @Column(nullable = false, unique = true)
    private String subModuleCode;   // e.g. STAFF_DIRECTORY

    @Column(nullable = false)
    private String subModuleName;      // Human readable, e.g. "Staff Directory"

    @Column(length = 1000)
    private String description;
}
