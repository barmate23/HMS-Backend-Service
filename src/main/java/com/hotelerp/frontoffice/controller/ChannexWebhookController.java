package com.hotelerp.frontoffice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.constants.ServiceConstants;
import com.hotelerp.frontoffice.service.ChannexWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller to receive push webhooks from Channex Channel Manager.
 * Accepts raw String body and parses as JsonNode for maximum resilience.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ChannexWebhookController {

    private final ChannexWebhookService channexWebhookService;
    private final ObjectMapper objectMapper;

    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.CHANNEX_WEBHOOK)
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Channex webhook endpoint is reachable",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @PostMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.CHANNEX_WEBHOOK)
    public ResponseEntity<StandardResponse<?>> handleChannexBookingWebhook(
            @RequestBody String rawBody) {

        log.info("══════════════════════════════════════════════════════════");
        log.info("CHANNEX RAW PAYLOAD: {}", rawBody);
        log.info("══════════════════════════════════════════════════════════");

        try {
            // Parse as JsonNode — guaranteed to work with ANY JSON structure
            JsonNode root = objectMapper.readTree(rawBody);

            log.info("Parsed fields: booking_unique_id={}, customer_name={}, arrival_date={}, count_of_nights={}",
                    root.path("booking_unique_id").asText(null),
                    root.path("customer_name").asText(null),
                    root.path("arrival_date").asText(null),
                    root.path("count_of_nights").asInt(0));

            StandardResponse<?> response = channexWebhookService.processBookingWebhook(root);
            log.info("Result: success={}, message={}", response.isSuccess(), response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to process Channex webhook. Raw: {}", rawBody, e);
            return ResponseEntity.status(500).body(
                    StandardResponse.error("Error: " + e.getMessage(), "CHANNEX_WEBHOOK_ERROR", e.toString())
            );
        }
    }
}
