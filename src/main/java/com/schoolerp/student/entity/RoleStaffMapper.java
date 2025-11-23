package com.schoolerp.student.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "roleStaffMapper")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleStaffMapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false)
    private boolean isDeleted;

}