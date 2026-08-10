package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    /** Parent reservation to check in */
    private Long reservationId;

    /** One item per booking/room to check in simultaneously */
    private List<BookingCheckInItem> bookings;

    private String idVerification;
    private String paymentMethod;

    /** Total amount being collected at check-in (applied to reservation folio) */
    private BigDecimal amountToSettle;
}
