package com.hotelerp.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "common_masters", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_code", columnList = "code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // e.g., SOP_FREQUENCY, AUDIT_AREA, RESPONSIBLE_ROLE

    @Column(name = "code", nullable = false, length = 50)
    private String code; // e.g., DAILY, BATHROOM, ROOM_ATTENDANT

    @Column(name = "value", nullable = false, length = 100)
    private String value; // e.g., Daily SOP, Bathroom, Room Attendant

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
