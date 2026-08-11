package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Reservation-level response for the arrivals/departures listing.
 * Each reservation contains a list of its associated bookings (rooms).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationArrivalResponse {
    private Long reservationId;
    private String reservationRef;
    /** System-generated booking confirmation number e.g. BK-2026-1108-0042 */
    private String confirmationNumber;

    // Guest info
    private String guestName;
    private Boolean guestIsVip;

    // Stay info
    private Integer numberOfNights;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalTime eta;

    // Financials — aggregate across all bookings in this reservation
    private BigDecimal totalBaseAmount;
    private BigDecimal paidAmount;
    private Integer gstPercent;

    // Overall status derived from bookings
    private String overallStatus;

    // Number of rooms
    private Integer numberOfRooms;

    // All bookings under this reservation (one per room)
    private List<ArrivalBookingResponse> bookings;
}
