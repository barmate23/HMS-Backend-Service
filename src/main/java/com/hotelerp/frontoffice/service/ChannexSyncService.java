package com.hotelerp.frontoffice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotelerp.frontoffice.common.StandardResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ChannexSyncService {

    /**
     * Pulls unacknowledged booking revisions from Channex Feed, creates HMS reservations,
     * and automatically acknowledges each processed revision.
     */
    StandardResponse<?> pullAndAckBookingFeed(String apiKeyOverride);

    /**
     * Sends acknowledgement for a specific revision ID to Channex API.
     */
    StandardResponse<?> acknowledgeRevision(String revisionId, String apiKeyOverride);

    /**
     * Pushes Availability & Rate (ARI) updates from HMS to Channex.
     */
    StandardResponse<?> pushAri(String propertyId, String roomTypeId, String ratePlanId,
                                LocalDate startDate, LocalDate endDate, Integer availability, BigDecimal rate,
                                String apiKeyOverride);

    /**
     * Auto-syncs all HMS active RoomTypes availability & rates to Channex for a date range.
     */
    StandardResponse<?> syncHmsAvailabilityToChannex(LocalDate startDate, LocalDate endDate, String apiKeyOverride);

    /**
     * Creates a new Room Type in Channex directly from HMS.
     */
    StandardResponse<?> createRoomTypeInChannex(String title, Integer countOfRooms, Integer capacity, String propertyId, String apiKeyOverride);

    /**
     * Creates a new Rate Plan in Channex directly from HMS for a specific Channex Room Type ID.
     */
    StandardResponse<?> createRatePlanInChannex(String title, String roomTypeId, BigDecimal rate, String currency, String propertyId, String apiKeyOverride);

    /**
     * Auto-syncs local HMS Room Types & Rate Plans to Channex (creates missing ones directly in Channex).
     */
    StandardResponse<?> syncHmsMasterToChannex(String propertyId, String apiKeyOverride);

    /**
     * Fetches properties from Channex API using key.
     */
    JsonNode getChannexProperties(String apiKeyOverride);

    /**
     * Fetches room types from Channex API for property.
     */
    JsonNode getChannexRoomTypes(String propertyId, String apiKeyOverride);

    /**
     * Fetches rate plans from Channex API for property.
     */
    JsonNode getChannexRatePlans(String propertyId, String apiKeyOverride);

    /**
     * Tests connection to Channex API using key and property ID.
     */
    StandardResponse<?> testConnection(String propertyId, String apiKeyOverride);

    /**
     * Creates a new Channel in Channex directly.
     */
    StandardResponse<?> createChannelInChannex(String title, String channelCode, String propertyId, String groupId, Boolean isActive, String apiKeyOverride);

    /**
     * Fetches channels configured in Channex for property.
     */
    JsonNode getChannexChannels(String propertyId, String apiKeyOverride);
}
