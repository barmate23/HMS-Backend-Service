package com.hotelerp.frontoffice.dto.channex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Channex customer (guest) object embedded inside a BookingRevision.
 *
 * The 'name' field is a full name string (e.g. "John Doe") which will be
 * split into firstName + lastName during processing.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannexCustomer {

    /** Full guest name, e.g. "John Doe" */
    @JsonProperty("name")
    private String name;

    /** Guest email address */
    @JsonProperty("mail")
    private String mail;

    /** Guest phone number (may include country code) */
    @JsonProperty("phone")
    private String phone;

    /** Guest address line */
    @JsonProperty("address")
    private String address;

    /** Guest city */
    @JsonProperty("city")
    private String city;

    /** Guest country */
    @JsonProperty("country")
    private String country;

    /** Guest zip/postal code */
    @JsonProperty("zip")
    private String zip;
}
