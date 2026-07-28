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
 * Controller to receive push webhooks & handle synchronization from Channex Channel Manager.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ChannexWebhookController {

    private final ChannexWebhookService channexWebhookService;
    private final ObjectMapper objectMapper;

    /**
     * GET Health Check / Ping
     */
    @GetMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.CHANNEX_WEBHOOK)
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("GET health-check hit on Channex webhook endpoint");
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Channex webhook endpoint is reachable",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * POST Webhook Receiver
     */
    @PostMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.CHANNEX_WEBHOOK)
    public ResponseEntity<StandardResponse<?>> handleChannexBookingWebhook(
            @RequestBody String rawBody) {

        log.info("══════════════════════════════════════════════════════════");
        log.info("CHANNEX WEBHOOK RAW PAYLOAD:");
        log.info("{}", rawBody);
        log.info("══════════════════════════════════════════════════════════");

        try {
            JsonNode root = parseJsonTreeSafely(rawBody);

            StandardResponse<?> response = channexWebhookService.processBookingWebhook(root);
            log.info("Webhook processing result: success={}, message={}", response.isSuccess(), response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to process Channex webhook. Raw body: {}", rawBody, e);
            return ResponseEntity.status(500).body(
                    StandardResponse.error("Error: " + e.getMessage(), "CHANNEX_WEBHOOK_ERROR", e.toString())
            );
        }
    }

    /**
     * POST Manual Sync Trigger Endpoint
     */
    @PostMapping(ServiceConstants.RESERVATION_BASE_URL + "/webhook/channex/sync")
    public ResponseEntity<StandardResponse<?>> syncChannexBooking(@RequestBody String rawBody) {
        log.info("Manual Channex Sync triggered with payload: {}", rawBody);
        try {
            JsonNode root = parseJsonTreeSafely(rawBody);
            StandardResponse<?> response = channexWebhookService.processBookingWebhook(root);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in manual Channex sync: ", e);
            return ResponseEntity.status(500).body(
                    StandardResponse.error("Sync failed: " + e.getMessage(), "CHANNEX_SYNC_ERROR", e.toString())
            );
        }
    }

    /**
     * Safely parse raw String body into JsonNode tree, handling double-stringified JSON gracefully
     */
    private JsonNode parseJsonTreeSafely(String rawBody) throws Exception {
        if (rawBody == null || rawBody.isBlank()) {
            throw new IllegalArgumentException("Raw request body is empty");
        }

        JsonNode node = objectMapper.readTree(rawBody);
        // If stringified JSON (starts/ends with quotes), unwrap second layer
        if (node.isTextual()) {
            String unquoted = node.asText();
            if (unquoted != null && unquoted.trim().startsWith("{")) {
                node = objectMapper.readTree(unquoted);
            }
        }
        return node;
    }
}
