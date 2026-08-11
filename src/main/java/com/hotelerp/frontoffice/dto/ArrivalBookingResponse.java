package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArrivalBookingResponse {
    private Long bookingId;
    private String bookingRef;
    private String guestName;
    private Boolean guestIsVip;
    private Integer numberOfNights;
    private String roomTypeName;
    private LocalTime eta;
    private BigDecimal baseAmount;
    private BigDecimal paidAmount;
    private Integer gstPercent;
    private String bookingStatus;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String roomNumber;
    private Long roomId;
}
