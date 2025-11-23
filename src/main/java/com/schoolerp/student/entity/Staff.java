package com.schoolerp.student.entity;


import com.schoolerp.student.constants.StaffStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    private LocalDate dob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    private String fatherName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffStatus status = StaffStatus.ACTIVE;

    @Column(nullable = false, unique = true, length = 20)
    private String staffCode; // e.g., ACC-2025-0007


    @Column
    @Lob
    private byte[] staffImage; // e.g., ACC-2025-0007



    @Column(nullable = false)
    private boolean isDeleted;


}
