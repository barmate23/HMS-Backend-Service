package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInPopupResponse {
    private Long bookingId;
    private String bookingRef;
    private String confirmationNumber;
    private String guestName;
    private String guestPhone;
    private Boolean guestIsVip;
    private LocalDate checkInDate;
    private LocalTime expectedArrival;
    private Integer numberOfNights;
    private String roomTypeName;
    private String occupancy;
    private String ratePlan;
    private String source;
    private BigDecimal totalEstBill;
    private BigDecimal balanceDue;
    private String assignedRoomNumber;
    private Long assignedRoomId;
    private List<AvailableRoomSummary> availableRooms;
}
