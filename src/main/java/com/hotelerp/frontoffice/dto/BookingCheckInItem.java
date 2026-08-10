package com.hotelerp.frontoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-booking room assignment item for a batch check-in request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCheckInItem {
    /** ID of the Booking row to check in */
    private Long bookingId;
    /** ID of the Room to assign for this booking */
    private Long roomId;
}
