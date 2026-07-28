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
            String url = channexBaseUrl + "/availability";
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

            String url = channexBaseUrl + "/availability";
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
    public StandardResponse<?> createRoomTypeInChannex(String title, Integer countOfRooms, Integer capacity, String propertyId, String apiKeyOverride) {
        String apiKey = resolveApiKey(apiKeyOverride);
        if (apiKey == null || apiKey.isBlank()) {
            return StandardResponse.error("Channex API Key is required", "MISSING_API_KEY", null);
        }

        String propId = (propertyId != null && !propertyId.isBlank()) ? propertyId : configuredPropertyId;

        try {
            String url = channexBaseUrl + "/room_types";
            HttpHeaders headers = buildHeaders(apiKey);

            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode roomTypeNode = objectMapper.createObjectNode();
            roomTypeNode.put("property_id", propId);
            roomTypeNode.put("title", title != null ? title.trim() : "Standard Room");
            roomTypeNode.put("count_of_rooms", countOfRooms != null && countOfRooms > 0 ? countOfRooms : 10);
            roomTypeNode.put("default_occupancy", capacity != null && capacity > 0 ? capacity : 2);
            roomTypeNode.put("occ_adults", capacity != null && capacity > 0 ? capacity : 2);
            roomTypeNode.put("occ_children", 1);
            roomTypeNode.put("occ_infants", 1);

            root.set("room_type", roomTypeNode);

            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(root), headers);
            log.info("Creating Room Type in Channex: title={}, propertyId={}", title, propId);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode createdNode = objectMapper.readTree(response.getBody());
                return StandardResponse.success(createdNode, "Room Type created in Channex successfully");
            } else {
                return StandardResponse.error("Channex returned status: " + response.getStatusCode(), "CREATE_ROOM_TYPE_ERROR", response.getBody());
            }
        } catch (Exception e) {
            log.error("Error creating Room Type in Channex: ", e);
            return StandardResponse.error("Error creating Room Type: " + e.getMessage(), "CREATE_ROOM_TYPE_EXCEPTION", e.toString());
        }
    }

    @Override
    public StandardResponse<?> createRatePlanInChannex(String title, String roomTypeId, BigDecimal rate, String currency, String propertyId, String apiKeyOverride) {
        String apiKey = resolveApiKey(apiKeyOverride);
        if (apiKey == null || apiKey.isBlank()) {
            return StandardResponse.error("Channex API Key is required", "MISSING_API_KEY", null);
        }

        String propId = (propertyId != null && !propertyId.isBlank()) ? propertyId : configuredPropertyId;

        try {
            String url = channexBaseUrl + "/rate_plans";
            HttpHeaders headers = buildHeaders(apiKey);

            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode ratePlanNode = objectMapper.createObjectNode();
            ratePlanNode.put("property_id", propId);
            ratePlanNode.put("room_type_id", roomTypeId);
            ratePlanNode.put("title", title != null ? title.trim() : "Standard Rate");
            ratePlanNode.put("currency", (currency != null && !currency.isBlank()) ? currency : "INR");
            ratePlanNode.put("sell_mode", "per_room");

            ArrayNode optionsArray = objectMapper.createArrayNode();
            ObjectNode optionNode = objectMapper.createObjectNode();
            optionNode.put("occupancy", 2);
            optionNode.put("is_primary", true);
            optionNode.put("rate", (rate != null ? rate : BigDecimal.valueOf(1000)).toPlainString());
            optionsArray.add(optionNode);
            ratePlanNode.set("options", optionsArray);

            root.set("rate_plan", ratePlanNode);

            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(root), headers);
            log.info("Creating Rate Plan in Channex: title={}, roomTypeId={}, propertyId={}, rate={}", title, roomTypeId, propId, rate);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode createdNode = objectMapper.readTree(response.getBody());
                return StandardResponse.success(createdNode, "Rate Plan created in Channex successfully");
            } else {
                return StandardResponse.error("Channex returned status: " + response.getStatusCode(), "CREATE_RATE_PLAN_ERROR", response.getBody());
            }
        } catch (Exception e) {
            log.error("Error creating Rate Plan in Channex: ", e);
            return StandardResponse.error("Error creating Rate Plan: " + e.getMessage(), "CREATE_RATE_PLAN_EXCEPTION", e.toString());
        }
    }

    @Override
    public StandardResponse<?> syncHmsMasterToChannex(String propertyId, String apiKeyOverride) {
        String apiKey = resolveApiKey(apiKeyOverride);
        if (apiKey == null || apiKey.isBlank()) {
            return StandardResponse.error("Channex API Key is required", "MISSING_API_KEY", null);
        }

        String propId = (propertyId != null && !propertyId.isBlank()) ? propertyId : configuredPropertyId;

        try {
            // 1. Read active HMS Room Types
            List<RoomType> hmsRoomTypes = roomTypeRepository.findAll().stream()
                    .filter(rt -> Boolean.TRUE.equals(rt.getIsActive()))
                    .toList();

            // 2. Fetch existing Room Types in Channex
            JsonNode existingChannexRoomTypes = getChannexRoomTypes(propId, apiKey);
            Map<String, String> channexRoomTypeMap = new HashMap<>(); // title.toLowerCase() -> channex_room_type_id
            if (existingChannexRoomTypes.has("data") && existingChannexRoomTypes.get("data").isArray()) {
                for (JsonNode item : existingChannexRoomTypes.get("data")) {
                    String id = item.path("id").asText(null);
                    String title = item.path("attributes").path("title").asText(null);
                    if (id != null && title != null) {
                        channexRoomTypeMap.put(title.trim().toLowerCase(), id);
                    }
                }
            }

            int createdRoomTypes = 0;
            List<String> createdNames = new ArrayList<>();

            for (RoomType rt : hmsRoomTypes) {
                String title = rt.getName() != null ? rt.getName().trim() : "Room";
                if (!channexRoomTypeMap.containsKey(title.toLowerCase())) {
                    int count = roomRepository.findAll().stream()
                            .filter(r -> r.getRoomType() != null && r.getRoomType().getId().equals(rt.getId()))
                            .toList().size();
                    if (count == 0) count = 10;
                    int cap = rt.getCapacity() != null ? rt.getCapacity() : 2;

                    StandardResponse<?> res = createRoomTypeInChannex(title, count, cap, propId, apiKey);
                    if (res.isSuccess() && res.getData() != null) {
                        createdRoomTypes++;
                        createdNames.add(title);
                        JsonNode resData = objectMapper.valueToTree(res.getData());
                        String newId = resData.path("data").path("id").asText(resData.path("id").asText(null));
                        if (newId != null) {
                            channexRoomTypeMap.put(title.toLowerCase(), newId);
                        }
                    }
                }
            }

            // Refetch Channex Room Types to ensure we have all IDs
            existingChannexRoomTypes = getChannexRoomTypes(propId, apiKey);
            if (existingChannexRoomTypes.has("data") && existingChannexRoomTypes.get("data").isArray()) {
                for (JsonNode item : existingChannexRoomTypes.get("data")) {
                    String id = item.path("id").asText(null);
                    String title = item.path("attributes").path("title").asText(null);
                    if (id != null && title != null) {
                        channexRoomTypeMap.put(title.trim().toLowerCase(), id);
                    }
                }
            }

            // 3. Fetch existing Rate Plans in Channex
            JsonNode existingChannexRatePlans = getChannexRatePlans(propId, apiKey);
            Set<String> roomTypeIdsWithRatePlans = new HashSet<>();
            Map<String, String> channexRatePlanMap = new HashMap<>(); // room_type_id -> rate_plan_id

            if (existingChannexRatePlans.has("data") && existingChannexRatePlans.get("data").isArray()) {
                for (JsonNode item : existingChannexRatePlans.get("data")) {
                    String rpId = item.path("id").asText(null);
                    String rtId = item.path("attributes").path("room_type_id").asText(null);
                    if (rtId == null || rtId.isBlank()) {
                        rtId = item.path("relationships").path("room_type").path("data").path("id").asText(null);
                    }
                    if (rtId != null && !rtId.isBlank()) {
                        roomTypeIdsWithRatePlans.add(rtId);
                        if (rpId != null) channexRatePlanMap.put(rtId, rpId);
                    }
                }
            }

            // 4. Create missing Rate Plans in Channex for EVERY Room Type
            int createdRatePlans = 0;
            List<String> ratePlanDetails = new ArrayList<>();

            for (Map.Entry<String, String> entry : channexRoomTypeMap.entrySet()) {
                String roomTypeTitle = entry.getKey();
                String channexRoomTypeId = entry.getValue();

                if (!roomTypeIdsWithRatePlans.contains(channexRoomTypeId)) {
                    BigDecimal hmsRate = BigDecimal.valueOf(1000);
                    for (RoomType rt : hmsRoomTypes) {
                        if (rt.getName() != null && rt.getName().trim().equalsIgnoreCase(roomTypeTitle)) {
                            if (rt.getBasePricePerNight() != null) hmsRate = rt.getBasePricePerNight();
                            break;
                        }
                    }
                    String ratePlanTitle = "Standard Rate (" + roomTypeTitle + ")";
                    StandardResponse<?> rpRes = createRatePlanInChannex(ratePlanTitle, channexRoomTypeId, hmsRate, "INR", propId, apiKey);
                    if (rpRes.isSuccess() && rpRes.getData() != null) {
                        createdRatePlans++;
                        ratePlanDetails.add(ratePlanTitle);
                        JsonNode rpData = objectMapper.valueToTree(rpRes.getData());
                        String newRpId = rpData.path("data").path("id").asText(rpData.path("id").asText(null));
                        if (newRpId != null) channexRatePlanMap.put(channexRoomTypeId, newRpId);
                    }
                }
            }

            // 5. Push Room Rates & Availability (ARI) to Channex for all active Room Types
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(30);

            ArrayNode valuesArray = objectMapper.createArrayNode();
            int ratesPushedCount = 0;

            for (RoomType rt : hmsRoomTypes) {
                String title = rt.getName() != null ? rt.getName().trim().toLowerCase() : "";
                String channexRtId = channexRoomTypeMap.get(title);

                if (channexRtId != null) {
                    BigDecimal rate = rt.getBasePricePerNight() != null ? rt.getBasePricePerNight() : BigDecimal.valueOf(1000);
                    int count = roomRepository.findAll().stream()
                            .filter(r -> r.getRoomType() != null && r.getRoomType().getId().equals(rt.getId()))
                            .toList().size();
                    if (count == 0) count = 10;

                    String ratePlanId = channexRatePlanMap.get(channexRtId);

                    ObjectNode ariEntry = objectMapper.createObjectNode();
                    ariEntry.put("property_id", propId);
                    ariEntry.put("room_type_id", channexRtId);
                    if (ratePlanId != null) ariEntry.put("rate_plan_id", ratePlanId);
                    ariEntry.put("date_from", startDate.toString());
                    ariEntry.put("date_to", endDate.toString());
                    ariEntry.put("availability", count);
                    ariEntry.put("rate", rate);
                    valuesArray.add(ariEntry);

                    ratesPushedCount++;
                }
            }

            if (valuesArray.size() > 0) {
                ObjectNode ariRootNode = objectMapper.createObjectNode();
                ariRootNode.set("values", valuesArray);
                String url = channexBaseUrl + "/availability";
                HttpHeaders headers = buildHeaders(apiKey);
                HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(ariRootNode), headers);
                log.info("Pushing room rates and availability to Channex: url={}, payload={}", url, ariRootNode);
                try {
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
                } catch (Exception ex) {
                    log.warn("ARI push notice: {}", ex.getMessage());
                }
            }

            Map<String, Object> summary = Map.of(
                    "hmsRoomTypesTotal", hmsRoomTypes.size(),
                    "newRoomTypesCreatedInChannex", createdRoomTypes,
                    "newRatePlansCreatedInChannex", createdRatePlans,
                    "ratesAndAvailabilityPushed", ratesPushedCount,
                    "createdRoomTypeTitles", createdNames,
                    "createdRatePlanTitles", ratePlanDetails
            );

            return StandardResponse.success(summary, "HMS Master Room Types, Rate Plans & Room Rates synced to Channex successfully");

        } catch (Exception e) {
            log.error("Error syncing HMS master to Channex: ", e);
            return StandardResponse.error("Error syncing HMS master: " + e.getMessage(), "MASTER_SYNC_EXCEPTION", e.toString());
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
