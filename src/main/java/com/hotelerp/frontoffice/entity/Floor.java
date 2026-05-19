package com.hotelerp.frontoffice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms", indexes = {
    @Index(name = "idx_hotel_id", columnList = "hotelId")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Floor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotelId")
    private Hotel hotel;
    
    @Column(name = "floorNumber", length = 20)
    private String floorNumber;

    @Column(name = "noOfRooms")
    private Integer noOfRooms;
    
    @Column(name = "telephone", length = 20)
    private String telephone;
    
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "isActive")
    private Boolean isActive = true;


}