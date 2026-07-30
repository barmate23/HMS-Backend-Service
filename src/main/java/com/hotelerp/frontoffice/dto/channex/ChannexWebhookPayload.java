package com.hotelerp.frontoffice.dto.channex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Real Channex webhook payload — a FLAT JSON object.
 *
 * Actual payload from Channex (as seen in webhook logs):
 * {
 *   "currency": "INR",
 *   "amount": "100.00",
 *   "channel_id": null,
 *   "property_id": "uuid",
 *   "booking_id": "uuid",
 *   "arrival_date": "2026-09-01",
 *   "booking_revision_id": "uuid",
 *   "live_feed_event_id": "uuid",
 *   "count_of_rooms": 1,
 *   "booking_unique_id": "GBB-123456",
 *   "count_of_nights": 1,
 *   "customer_name": "Genz youth youth",
 *   "ota_code": "123456"
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannexWebhookPayload {

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("property_id")
    private String propertyId;

    @JsonProperty("booking_id")
    private String bookingId;

    @JsonProperty("arrival_date")
    private String arrivalDate;

    @JsonProperty("departure_date")
    private String departureDate;

    @JsonProperty("booking_revision_id")
    private String bookingRevisionId;

    @JsonProperty("live_feed_event_id")
    private String liveFeedEventId;

    @JsonProperty("count_of_rooms")
    private Integer countOfRooms;

    @JsonProperty("booking_unique_id")
    private String bookingUniqueId;

    @JsonProperty("count_of_nights")
    private Integer countOfNights;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("customer_phone")
    private String customerPhone;

    @JsonProperty("ota_code")
    private String otaCode;

    @JsonProperty("ota_name")
    private String otaName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("notes")
    private String notes;

    // ── Legacy nested structure support (if ever sent) ──
    @JsonProperty("event")
    private String event;
}
