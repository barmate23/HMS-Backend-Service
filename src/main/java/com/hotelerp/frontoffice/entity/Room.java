package com.hotelerp.frontoffice.entity;
 
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "rooms", indexes = {
    @Index(name = "idx_floor_id", columnList = "floorId"),
    @Index(name = "idx_room_number", columnList = "roomNumber"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_type_id", columnList = "typeId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    

    @Column(name = "roomNumber", nullable = false, length = 20)
    private String roomNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floorId", nullable = false)
    private Floor floor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typeId", nullable = false)
    private RoomType roomType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status = RoomStatus.VACANT;
    
    @Column(name = "maxOccupancy", nullable = false)
    private Integer maxOccupancy;
    
    @Column(name = "telephone", length = 20)
    private String telephone;
    
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "isActive")
    private Boolean isActive = true;

    // Enum for Room Status
    public enum RoomStatus {
        VACANT, OCCUPIED, MAINTENANCE, RESERVED, CLEANING
    }
}