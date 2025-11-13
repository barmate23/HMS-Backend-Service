package com.schoolerp.student.entity;

import com.schoolerp.staff.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "module_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    private boolean canView;
    private boolean canCreate;
    private boolean canEdit;
    private boolean canDelete;
}
