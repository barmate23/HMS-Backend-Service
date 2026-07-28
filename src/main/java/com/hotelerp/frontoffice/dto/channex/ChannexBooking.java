package com.hotelerp.frontoffice.dto.channex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Represents a Channex BookingRevision object.
 *
 * Key fields:
 *  - id                   → Channex internal booking UUID
 *  - status               → new | modified | cancelled
 *  - ota_name             → source OTA (Booking.com, Airbnb, Expedia, etc.)
 *  - ota_reservation_code → OTA confirmation code (used as bookingReference)
 *  - arrival_date         → check-in date (YYYY-MM-DD)
 *  - departure_date       → check-out date (YYYY-MM-DD)
 *  - amount               → total booking amount (string)
 *  - currency             → e.g. "INR"
 *  - customer             → guest info object
 *  - rooms                → list of booked room objects
 *  - notes                → special requests / guest notes
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannexBooking {

    /** Channex unique booking ID (UUID) */
    @JsonProperty("id")
    private String id;

    /**
     * Booking lifecycle status.
     * Values: "new" | "modified" | "cancelled"
     */
    @JsonProperty("status")
    private String status;

    /** Source OTA name, e.g. "Booking.com", "Airbnb" */
    @JsonProperty("ota_name")
    private String otaName;

    /** OTA-side confirmation/reservation code */
    @JsonProperty("ota_reservation_code")
    private String otaReservationCode;

    /** Check-in date as ISO string (YYYY-MM-DD) */
    @JsonProperty("arrival_date")
    private String arrivalDate;

    /** Check-out date as ISO string (YYYY-MM-DD) */
    @JsonProperty("departure_date")
    private String departureDate;

    /** Total booking amount as string (to be parsed to BigDecimal) */
    @JsonProperty("amount")
    private String amount;

    /** Currency code, e.g. "INR", "USD" */
    @JsonProperty("currency")
    private String currency;

    /** Guest/customer info object */
    @JsonProperty("customer")
    private ChannexCustomer customer;

    /** List of rooms in this booking (one per room type booked) */
    @JsonProperty("rooms")
    private List<ChannexRoom> rooms;

    /** Guest notes / special requests */
    @JsonProperty("notes")
    private String notes;

    /** Channex unique booking code (e.g. GBB-1234) */
    @JsonProperty("unique_id")
    private String uniqueId;

    /** Property ID in Channex */
    @JsonProperty("property_id")
    private String propertyId;

    /** Occupancy details (adults, children, infants) */
    @JsonProperty("occupancy")
    private ChannexOccupancy occupancy;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannexOccupancy {
        @JsonProperty("adults")
        private Integer adults;

        @JsonProperty("children")
        private Integer children;

        @JsonProperty("infants")
        private Integer infants;
    }
}
