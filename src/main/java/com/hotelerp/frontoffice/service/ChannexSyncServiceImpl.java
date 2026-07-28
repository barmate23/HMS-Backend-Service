package com.hotelerp.frontoffice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.entity.RoomType;
import com.hotelerp.frontoffice.repository.RoomRepository;
import com.hotelerp.frontoffice.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannexSyncServiceImpl implements ChannexSyncService {

    private final ChannexWebhookService channexWebhookService;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final ObjectMapper objectMapper;

    @Value("${channex.api.url:https://staging.channex.io/api/v1}")
    private String channexBaseUrl;

    @Value("${channex.api.key:}")
    private String configuredApiKey;

    @Value("${channex.property-id:3ac4f491-34c0-462e-95fd-acdf20006bdb}")
    private String configuredPropertyId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public StandardResponse<?> pullAndAckBookingFeed(String apiKeyOverride) {
        String apiKey = resolveApiKey(apiKeyOverride);
        if (apiKey == null || apiKey.isBlank()) {
            return StandardResponse.error("Channex API Key is required. Pass via Header 'user-api-key' or set CHANNEX_API_KEY", "MISSING_API_KEY", null);
        }

        try {
            String url = channexBaseUrl + "/booking_revisions/feed";
            HttpHeaders headers = buildHeaders(apiKey);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            log.info("Fetching booking revision feed from Channex: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataArray = root.path("data");

                int processedCount = 0;
                int ackCount = 0;
                List<String> processedRefs = new ArrayList<>();

                if (dataArray.isArray()) {
                    for (JsonNode item : dataArray) {
                        JsonNode revision = item.path("attributes");
                        String revisionId = item.path("id").asText(null);

                        // Fallback: if attributes is missing, use item itself
                        if (revision.isMissingNode() || revision.isNull()) {
                            revision = item;
                        }

                        StandardResponse<?> result = channexWebhookService.processBookingWebhook(revision);
                        if (result.isSuccess()) {
                            processedCount++;
                            String ref = revision.path("booking_unique_id").asText(revisionId);
                            processedRefs.add(ref);

                            // Acknowledge revision to Channex so it doesn't repeat
                            if (revisionId != null && !revisionId.isBlank()) {
                                boolean ackOk = executeAck(revisionId, apiKey);
                                if (ackOk) ackCount++;
                            }
                        }
                    }
                }

                Map<String, Object> summary = Map.of(
                        "totalFetched", dataArray.isArray() ? dataArray.size() : 0,
                        "processedCount", processedCount,
                        "acknowledgedCount", ackCount,
                        "bookingReferences", processedRefs
                );

                return StandardResponse.success(summary, "Booking revision feed pulled and acknowledged successfully");
            } else {
                return StandardResponse.error("Channex Feed returned status: " + response.getStatusCode(), "CHANNEX_FEED_ERROR", null);
            }
        } catch (Exception e) {
            log.error("Error pulling booking revision feed from Channex: ", e);
            return StandardResponse.error("Error pulling feed: " + e.getMessage(), "CHANNEX_FEED_EXCEPTION", e.toString());
        }
    }

    @Override
    public StandardResponse<?> acknowledgeRevision(String revisionId, String apiKeyOverride) {
        String apiKey = resolveApiKey(apiKeyOverride);
        if (apiKey == null || apiKey.isBlank()) {
            return StandardResponse.error("Channex API Key is required", "MISSING_API_KEY", null);
        }

        boolean success = executeAck(revisionId, apiKey);
        if (success) {
            return StandardResponse.success(Map.of("revisionId", revisionId, "acknowledged", true), "Revision acknowledged");
        } else {
            return StandardResponse.error("Failed to acknowledge revision " + revisionId, "ACK_FAILED", null);
        }
    }

    @Override
    public StandardResponse<?> pushAri(String propertyId, String roomTypeId, String ratePlanId,
                                       LocalDate startDate, LocalDate endDate, Integer availability, BigDecimal rate,
                                       String apiKeyOverride) {
        String apiKey = resolveApiKey(apiKeyOverride);
        if (apiKey == null || apiKey.isBlank()) {
            return StandardResponse.error("Channex API Key is required", "MISSING_API_KEY", null);
        }

        String targetPropertyId = (propertyId != null && !propertyId.isBlank()) ? propertyId : configuredPropertyId;

        try {
            String url = channexBaseUrl + "/ari";
            HttpHeaders headers = buildHeaders(apiKey);

            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode valuesArray = objectMapper.createArrayNode();

            ObjectNode ariEntry = objectMapper.createObjectNode();
            ariEntry.put("property_id", targetPropertyId);
            if (roomTypeId != null) ariEntry.put("room_type_id", roomTypeId);
            if (ratePlanId != null) ariEntry.put("rate_plan_id", ratePlanId);
            ariEntry.put("date_from", startDate.toString());
            ariEntry.put("date_to", endDate.toString());
            if (availability != null) ariEntry.put("availability", availability);
            if (rate != null) ariEntry.put("rate", rate);

            valuesArray.add(ariEntry);
            rootNode.set("values", valuesArray);

            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(rootNode), headers);
            log.info("Pushing ARI to Channex: url={}, payload={}", url, rootNode);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return StandardResponse.success(response.getBody(), "ARI update pushed to Channex successfully");
            } else {
                return StandardResponse.error("Channex ARI returned status: " + response.getStatusCode(), "ARI_PUSH_ERROR", response.getBody());
            }
        } catch (Exception e) {
            log.error("Error pushing ARI to Channex: ", e);
            return StandardResponse.error("Error pushing ARI: " + e.getMessage(), "ARI_PUSH_EXCEPTION", e.toString());
        }
    }

    @Override
    public StandardResponse<?> syncHmsAvailabilityToChannex(LocalDate startDate, LocalDate endDate, String apiKeyOverride) {
        String apiKey = resolveApiKey(apiKeyOverride);
        if (apiKey == null || apiKey.isBlank()) {
            return StandardResponse.error("Channex API Key is required", "MISSING_API_KEY", null);
        }

        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = startDate.plusDays(30);

        try {
            List<RoomType> activeRoomTypes = roomTypeRepository.findAll().stream()
                    .filter(rt -> Boolean.TRUE.equals(rt.getIsActive()))
                    .toList();

            ArrayNode valuesArray = objectMapper.createArrayNode();

            for (RoomType rt : activeRoomTypes) {
                // Calculate available count for this room type
                int availableRoomsCount = roomRepository.findAvailableRooms(startDate, endDate).stream()
                        .filter(r -> r.getRoomType() != null && r.getRoomType().getId().equals(rt.getId()))
                        .toList().size();

                BigDecimal basePrice = rt.getBasePricePerNight() != null ? rt.getBasePricePerNight() : BigDecimal.valueOf(1000);

                ObjectNode ariEntry = objectMapper.createObjectNode();
                ariEntry.put("property_id", configuredPropertyId);
                ariEntry.put("date_from", startDate.toString());
                ariEntry.put("date_to", endDate.toString());
                ariEntry.put("availability", availableRoomsCount);
                ariEntry.put("rate", basePrice);
                valuesArray.add(ariEntry);
            }

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.set("values", valuesArray);

            String url = channexBaseUrl + "/ari";
            HttpHeaders headers = buildHeaders(apiKey);
            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(rootNode), headers);

            log.info("Syncing HMS availability to Channex for {} room types ({} to {})", activeRoomTypes.size(), startDate, endDate);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return StandardResponse.success(Map.of(
                        "roomTypesSynced", activeRoomTypes.size(),
                        "startDate", startDate.toString(),
                        "endDate", endDate.toString(),
                        "channexResponse", response.getBody()
                ), "HMS Availability & Rates synced to Channex successfully");
            } else {
                return StandardResponse.error("Channex ARI returned status: " + response.getStatusCode(), "SYNC_ERROR", response.getBody());
            }

        } catch (Exception e) {
            log.error("Error syncing HMS availability to Channex: ", e);
            return StandardResponse.error("Error syncing availability: " + e.getMessage(), "SYNC_EXCEPTION", e.toString());
        }
    }

    @Override
    public JsonNode getChannexProperties(String apiKeyOverride) {
        return fetchJsonFromChannex("/properties", apiKeyOverride);
    }

    @Override
    public JsonNode getChannexRoomTypes(String propertyId, String apiKeyOverride) {
        String propId = (propertyId != null && !propertyId.isBlank()) ? propertyId : configuredPropertyId;
        return fetchJsonFromChannex("/room_types?filter[property_id]=" + propId, apiKeyOverride);
    }

    @Override
    public JsonNode getChannexRatePlans(String propertyId, String apiKeyOverride) {
        String propId = (propertyId != null && !propertyId.isBlank()) ? propertyId : configuredPropertyId;
        return fetchJsonFromChannex("/rate_plans?filter[property_id]=" + propId, apiKeyOverride);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ══════════════════════════════════════════════════════════════════════════

    private boolean executeAck(String revisionId, String apiKey) {
        try {
            String url = channexBaseUrl + "/booking_revisions/" + revisionId + "/ack";
            HttpHeaders headers = buildHeaders(apiKey);
            HttpEntity<String> requestEntity = new HttpEntity<>("{}", headers);

            log.info("Sending ACK for booking revision {} to Channex: {}", revisionId, url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to ACK revision {} on Channex: {}", revisionId, e.getMessage());
            return false;
        }
    }

    private JsonNode fetchJsonFromChannex(String path, String apiKeyOverride) {
        String apiKey = resolveApiKey(apiKeyOverride);
        if (apiKey == null || apiKey.isBlank()) {
            return objectMapper.createObjectNode().put("error", "Missing Channex API key");
        }

        try {
            String url = channexBaseUrl + path;
            HttpHeaders headers = buildHeaders(apiKey);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            log.error("Error fetching from Channex path {}: ", path, e);
        }
        return objectMapper.createObjectNode().put("error", "Failed to fetch from Channex path: " + path);
    }

    private HttpHeaders buildHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("user-api-key", apiKey);
        return headers;
    }

    private String resolveApiKey(String override) {
        if (override != null && !override.isBlank()) return override.trim();
        if (configuredApiKey != null && !configuredApiKey.isBlank()) return configuredApiKey.trim();
        return null;
    }
}
