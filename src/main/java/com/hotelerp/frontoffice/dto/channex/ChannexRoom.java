package com.hotelerp.frontoffice.dto.channex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Room line item received within a Channex Booking Revision payload.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannexRoom {

    /** Channex room revision ID */
    @JsonProperty("id")
    private String id;

    /** Title/Name of room type from OTA, e.g. "Deluxe King Room" */
    @JsonProperty("title")
    private String title;

    /** Room type ID configured in Channex (if mapped) */
    @JsonProperty("room_type_id")
    private String roomTypeId;

    /** Check-in date for this room line (ISO string YYYY-MM-DD) */
    @JsonProperty("checkin_date")
    private String checkinDate;

    /** Check-out date for this room line (ISO string YYYY-MM-DD) */
    @JsonProperty("checkout_date")
    private String checkoutDate;

    /** Price charged for this room line */
    @JsonProperty("rate")
    private String rate;

    @JsonProperty("adults_count")
    private Integer adultsCount;

    @JsonProperty("children_count")
    private Integer childrenCount;
}
