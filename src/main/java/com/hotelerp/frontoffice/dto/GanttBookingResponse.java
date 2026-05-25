package com.hotelerp.frontoffice.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GanttBookingResponse {
    private Long bookingId;
    private Long reservationId;
    private String reservationRef;
    private Long roomId;
    private String roomNumber;
    private String roomTypeName;
    private String guestName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private String color; // Optional: for UI differentiation if needed
}
