package com.schoolerp.staff.entity;

import com.schoolerp.staff.constants.StaffStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
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
    private String staffCode;

    @Column(length = 100)
    private String licenseNumber;

    @Lob
    private byte[] staffImage;

    @Column(nullable = false)
    private boolean isDeleted;

    /* ================= BANK DETAILS ================= */

    @Column(length = 100)
    private String bankName;

    @Column(length = 100)
    private String accountHolderName;

    @Column(length = 30)
    private String accountNumber;

    @Column(length = 20)
    private String ifscCode;

    @Column(length = 100)
    private String branchName;

    @Column(length = 100)
    private String upiId;

    /* ================= ADDRESS DETAILS ================= */

    @Column(length = 255)
    private String addressLine1;

    @Column(length = 255)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

}
