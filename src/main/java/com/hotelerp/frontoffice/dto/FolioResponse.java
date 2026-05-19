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
public class FolioResponse {
    private String bookingRef;
    private String guestName;
    private String guestPhone;
    private String roomNumber;
    private List<FolioTransaction> transactions;
    private BigDecimal totalCharges;
    private BigDecimal totalPayments;
    private BigDecimal currentBalance;
}
