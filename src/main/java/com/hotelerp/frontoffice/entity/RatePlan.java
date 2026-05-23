package com.hotelerp.frontoffice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate_plan_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // Plan name (UI display)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Description (Room Only, Room + Breakfast, etc.)
    @Column(name = "description")
    private String description;

    // Price adjustment (can be +500 or 10%)
    @Column(name = "price_adjustment", nullable = false)
    private BigDecimal priceAdjustment;


    // UI order
    @Column(name = "display_order")
    private Integer displayOrder;

    // Active flag
    @Column(name = "is_active")
    private Boolean isActive;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}