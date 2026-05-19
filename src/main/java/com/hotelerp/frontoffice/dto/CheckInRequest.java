package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    private Long bookingId;
    private Long roomId;
    private String idVerification;
    private String paymentMethod;
    private BigDecimal amountToSettle;
}
