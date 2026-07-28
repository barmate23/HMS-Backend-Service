package com.hotelerp.frontoffice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.constants.ServiceConstants;
import com.hotelerp.frontoffice.service.ChannexSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Controller for bidirectional Channex Synchronization:
 *  - Pulling & Acknowledging Booking Revisions Feed
 *  - Pushing ARI (Availability, Rates, Restrictions) from HMS to Channex
 *  - Querying Channex Properties, Room Types, and Rate Plans
 */
@RestController
@RequestMapping(ServiceConstants.RESERVATION_BASE_URL + "/channex")
@RequiredArgsConstructor
@Slf4j
public class ChannexSyncController {

    private final ChannexSyncService channexSyncService;

    /**
     * Pull unacknowledged booking revisions from Channex Feed, save them as HMS reservations,
     * and automatically acknowledge them.
     */
    @PostMapping("/feed/pull")
    public ResponseEntity<StandardResponse<?>> pullBookingFeed(
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        log.info("Pulling booking feed from Channex...");
        StandardResponse<?> response = channexSyncService.pullAndAckBookingFeed(apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Acknowledge a specific revision ID on Channex.
     */
    @PostMapping("/ack/{revisionId}")
    public ResponseEntity<StandardResponse<?>> acknowledgeRevision(
            @PathVariable("revisionId") String revisionId,
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        log.info("Acknowledging revisionId={} on Channex...", revisionId);
        StandardResponse<?> response = channexSyncService.acknowledgeRevision(revisionId, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Push Availability, Rates & Restrictions (ARI) to Channex for specific room type / rate plan.
     */
    @PostMapping("/ari/push")
    public ResponseEntity<StandardResponse<?>> pushAri(
            @RequestParam(value = "propertyId", required = false) String propertyId,
            @RequestParam(value = "roomTypeId", required = false) String roomTypeId,
            @RequestParam(value = "ratePlanId", required = false) String ratePlanId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "availability", required = false) Integer availability,
            @RequestParam(value = "rate", required = false) BigDecimal rate,
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        log.info("Pushing ARI update to Channex for propertyId={}, roomTypeId={}, dates={} to {}", propertyId, roomTypeId, startDate, endDate);
        StandardResponse<?> response = channexSyncService.pushAri(propertyId, roomTypeId, ratePlanId, startDate, endDate, availability, rate, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Auto-Sync all HMS active RoomTypes availability & base rates to Channex for a date range.
     */
    @PostMapping("/ari/sync-hms")
    public ResponseEntity<StandardResponse<?>> syncHmsAvailabilityToChannex(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        log.info("Syncing all HMS room availability to Channex...");
        StandardResponse<?> response = channexSyncService.syncHmsAvailabilityToChannex(startDate, endDate, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new Room Type directly in Channex.
     */
    @PostMapping("/room-type/create")
    public ResponseEntity<StandardResponse<?>> createRoomType(
            @RequestParam("title") String title,
            @RequestParam(value = "countOfRooms", required = false) Integer countOfRooms,
            @RequestParam(value = "capacity", required = false) Integer capacity,
            @RequestParam(value = "propertyId", required = false) String propertyId,
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        log.info("Creating Room Type in Channex: title={}", title);
        StandardResponse<?> response = channexSyncService.createRoomTypeInChannex(title, countOfRooms, capacity, propertyId, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new Rate Plan directly in Channex for a Channex Room Type.
     */
    @PostMapping("/rate-plan/create")
    public ResponseEntity<StandardResponse<?>> createRatePlan(
            @RequestParam("title") String title,
            @RequestParam("roomTypeId") String roomTypeId,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "propertyId", required = false) String propertyId,
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        log.info("Creating Rate Plan in Channex: title={}, roomTypeId={}", title, roomTypeId);
        StandardResponse<?> response = channexSyncService.createRatePlanInChannex(title, roomTypeId, currency, propertyId, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Auto-sync all active HMS Room Types & Rate Plans to Channex (creates missing ones in Channex).
     */
    @PostMapping("/master/sync")
    public ResponseEntity<StandardResponse<?>> syncMaster(
            @RequestParam(value = "propertyId", required = false) String propertyId,
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        log.info("Syncing HMS Master (Room Types & Rate Plans) to Channex...");
        StandardResponse<?> response = channexSyncService.syncHmsMasterToChannex(propertyId, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch properties configured under your Channex account.
     */
    @GetMapping("/properties")
    public ResponseEntity<JsonNode> getProperties(
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        JsonNode response = channexSyncService.getChannexProperties(apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch Room Types configured in Channex for property.
     */
    @GetMapping("/room-types")
    public ResponseEntity<JsonNode> getRoomTypes(
            @RequestParam(value = "propertyId", required = false) String propertyId,
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        JsonNode response = channexSyncService.getChannexRoomTypes(propertyId, apiKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch Rate Plans configured in Channex for property.
     */
    @GetMapping("/rate-plans")
    public ResponseEntity<JsonNode> getRatePlans(
            @RequestParam(value = "propertyId", required = false) String propertyId,
            @RequestHeader(value = "user-api-key", required = false) String apiKeyHeader,
            @RequestParam(value = "apiKey", required = false) String apiKeyParam) {

        String apiKey = resolveKey(apiKeyHeader, apiKeyParam);
        JsonNode response = channexSyncService.getChannexRatePlans(propertyId, apiKey);
        return ResponseEntity.ok(response);
    }

    private String resolveKey(String headerKey, String paramKey) {
        if (headerKey != null && !headerKey.isBlank()) return headerKey.trim();
        if (paramKey != null && !paramKey.isBlank()) return paramKey.trim();
        return null;
    }
}
