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

    /** Guest first name */
    @JsonProperty("name")
    private String name;

    /** Guest last name / surname */
    @JsonProperty("surname")
    private String surname;

    /** Guest email address (Channex uses "email" not "mail") */
    @JsonProperty("email")
    private String email;

    /** Also support "mail" as some OTAs use this key */
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

    /** Convenience method to get email regardless of which field Channex used */
    public String getResolvedEmail() {
        if (email != null && !email.isBlank()) return email;
        if (mail != null && !mail.isBlank()) return mail;
        return null;
    }

    /** Convenience method to get full name */
    public String getFullName() {
        if (name != null && surname != null) return name.trim() + " " + surname.trim();
        if (name != null) return name.trim();
        return "OTA Guest";
    }
}
