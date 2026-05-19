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
public class CheckOutRequest {
    private Long bookingId;
    
    // Clearance Step
    private Boolean keysReturned;
    private BigDecimal lateCheckOutFee;
    private BigDecimal minibarCharges;
    private Boolean roomDamageReported;
    private BigDecimal damagePenaltyCharge;
    private String damageDescription;

    // Settlement Step
    private String paymentMethod;
    private BigDecimal amountToCollect;

    // Departure Step
    private String transportationRequested;
    private String guestFeedback;
}
