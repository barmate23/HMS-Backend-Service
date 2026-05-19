package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolioTransaction {
    private LocalDate date;
    private String description;
    private BigDecimal charges;
    private BigDecimal payments;
    private String type; // CHARGE or PAYMENT
}
