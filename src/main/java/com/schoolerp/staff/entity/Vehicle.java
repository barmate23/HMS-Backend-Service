package com.schoolerp.staff.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String vehicleNumber;

    @Column(nullable = false)
    private String model;

    private Integer capacity;

    private Integer status; // Active, Inactive, Maintenance

    private String fuelType; // Diesel, Petrol, CNG

    private LocalDate insuranceExpiry;

    private LocalDate lastServiceDate;

    private LocalDate nextServiceDate;

    private Boolean isDeleted = false;
}
