package com.schoolerp.staff.entity;


import com.schoolerp.staff.constants.StaffStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String designation;

    @Column(nullable = false)
    private boolean isStaff;

    @Column(nullable = false)
    private boolean isDeleted;

    @Column(nullable = false)
    private boolean isDefaultPasswordGenerated;


}
