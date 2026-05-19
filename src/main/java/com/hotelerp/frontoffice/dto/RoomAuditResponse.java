package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAuditResponse {
    private Long id;
    private Long roomId;
    private String roomNumber;
    private Long bookingId;
    private String operationType;
    private BigDecimal amountPaid;
    private String notes;
    private LocalDateTime createdAt;
}
