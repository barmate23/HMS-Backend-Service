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
public class CheckOutRequest {
    /** Parent reservation to check out */
    private Long reservationId;

    /** One or more booking IDs to check out simultaneously */
    private List<Long> bookingIds;

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
