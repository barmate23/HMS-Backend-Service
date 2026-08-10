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
public class ArrivalsListResponse {
    /** Reservations grouped with their bookings (one entry per reservation) */
    private List<ReservationArrivalResponse> reservations;
    private Long pendingArrivalsCount;
    private Long checkedInCount;
    private Long totalExpectedCount;
}
