package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusResponse {
    private Long id;
    private String roomNumber;
    private Long floorId;
    private String floorNumber;
    private Long roomTypeId;
    private String roomTypeName;
    private String status;
    private Integer maxOccupancy;
    private String telephone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
}
