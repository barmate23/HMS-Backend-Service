package com.schoolerp.student.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "module_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

   @ManyToOne
   @JoinColumn(name = "sub_module_id", nullable = false)
   private SubModule subModule;

    private boolean canView;
    private boolean canCreate;
    private boolean canEdit;
    private boolean canDelete;
}
