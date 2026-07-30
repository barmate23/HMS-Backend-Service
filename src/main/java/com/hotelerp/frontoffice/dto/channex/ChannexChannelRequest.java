package com.hotelerp.frontoffice.dto.channex;

import lombok.*;

/**
 * Request payload for creating a Channel in Channex.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannexChannelRequest {
    private String title;
    private String channelCode; // e.g. "booking_com", "expedia", "agoda", "airbnb", "makemytrip"
    private String propertyId;
    private String groupId;
    @Builder.Default
    private Boolean isActive = true;
    private String apiKey;
}
