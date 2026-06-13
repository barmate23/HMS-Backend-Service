package com.hotelerp.frontoffice.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * Lightweight Room projection returned in the available-rooms listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private Long   id;
    private String roomNumber;
    private String floor;

    // Room type
    private Long       roomTypeId;
    private String     roomTypeName;
    private BigDecimal basePricePerNight;

    // Details
    private Integer    maxOccupancy;
    private String status;
    private Boolean    isActive;
}
