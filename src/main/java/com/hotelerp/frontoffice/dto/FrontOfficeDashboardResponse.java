package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontOfficeDashboardResponse {
    private LocalDate businessDate;
    private Summary summary;
    private List<FloorBoard> floors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalRooms;
        private int totalBookings;
        private int availableRooms;
        private int occupiedRooms;
        private int bookedRooms;
        private int blockedRooms;
        private int underMaintenanceRooms;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FloorBoard {
        private Long floorId;
        private String floorName;
        private int totalRooms;
        private int availableRooms;
        private int occupiedRooms;
        private int bookedRooms;
        private int blockedRooms;
        private int underMaintenanceRooms;
        private List<RoomCard> rooms;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomCard {
        private Long roomId;
        private String roomNumber;
        private Long floorId;
        private String floorName;
        private String roomType;
        private Integer maxOccupancy;
        private String roomStatus;
        private String housekeepingStatus;
        private String displayStatus;
        private BookingSnapshot booking;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingSnapshot {
        private Long bookingId;
        private Long reservationId;
        private String reservationRef;
        private String guestName;
        private String guestPhone;
        private String guestEmail;
        private Boolean vip;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer nights;
        private Integer adults;
        private Integer children;
        private String reservationStatus;
        private String bookingStatus;
        private String ratePlanName;
        private BigDecimal ratePerNight;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private String billingName;
        private String billingMode;
        private String businessSource;
        private String marketSegment;
        private String specialRequests;
        private String notes;
    }
}
