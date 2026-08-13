package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GanttChartResponse {
    private Summary summary;
    private List<GanttBookingResponse> bookings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalBookings;
        private int occupiedRooms;
        private int checkedIn;
        /** Number of rooms booked per reservation, grouped by confirmation number */
        private List<ReservationSummary> reservationSummaries;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationSummary {
        private Long reservationId;
        private String confirmationNumber;
        private String guestName;
        private int roomCount;
    }
}
