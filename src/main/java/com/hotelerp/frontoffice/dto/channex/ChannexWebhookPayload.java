package com.hotelerp.frontoffice.dto.channex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Top-level wrapper received from Channex webhook.
 *
 * Channex POST body structure:
 * {
 *   "event": "booking",
 *   "payload": {
 *     "booking": { ... }
 *   }
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannexWebhookPayload {

    /** Event type, e.g. "booking" */
    @JsonProperty("event")
    private String event;

    /** Nested payload wrapper */
    @JsonProperty("payload")
    private ChannexPayloadWrapper payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannexPayloadWrapper {

        @JsonProperty("booking")
        private ChannexBooking booking;
    }
}
