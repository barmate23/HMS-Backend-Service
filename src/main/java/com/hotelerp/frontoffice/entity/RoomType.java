package com.hotelerp.frontoffice.entity;
 
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "room_types", indexes = {
    @Index(name = "idx_hotel_id", columnList = "hotelId"),
    @Index(name = "idx_active", columnList = "isActive")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotelId", nullable = false)
    private Hotel hotel;
    
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Column(name = "basePricePerNight", nullable = false, 
            precision = 10, scale = 2)
    private BigDecimal basePricePerNight;
    
    @Column(name = "area", nullable = false, precision = 8, scale = 2)
    private BigDecimal area;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "imageUrl", length = 500)
    private String imageUrl;
    
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "isActive")
    private Boolean isActive = true;

}
      