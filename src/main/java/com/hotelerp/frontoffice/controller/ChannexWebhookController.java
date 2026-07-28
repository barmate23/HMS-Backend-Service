package com.hotelerp.frontoffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.constants.ServiceConstants;
import com.hotelerp.frontoffice.dto.channex.ChannexWebhookPayload;
import com.hotelerp.frontoffice.service.ChannexWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller to receive push webhooks from Channex Channel Manager.
 * Accepts raw String body to log exact payload before parsing.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ChannexWebhookController {

    private final ChannexWebhookService channexWebhookService;
    private final ObjectMapper objectMapper;

    /**
     * GET health-check — verify the webhook route is reachable.
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
     * POST webhook receiver — accepts raw String body to log exact Channex payload,
     * then parses into flat DTO for processing.
     */
    @PostMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.CHANNEX_WEBHOOK)
    public ResponseEntity<StandardResponse<?>> handleChannexBookingWebhook(
            @RequestBody String rawBody) {

        log.info("══════════════════════════════════════════════════════════");
        log.info("CHANNEX WEBHOOK RAW PAYLOAD:");
        log.info("{}", rawBody);
        log.info("══════════════════════════════════════════════════════════");

        try {
            ChannexWebhookPayload payload = objectMapper.readValue(rawBody, ChannexWebhookPayload.class);

            log.info("Parsed Channex payload: bookingUniqueId={}, customerName={}, arrivalDate={}, nights={}, rooms={}, amount={}",
                    payload.getBookingUniqueId(),
                    payload.getCustomerName(),
                    payload.getArrivalDate(),
                    payload.getCountOfNights(),
                    payload.getCountOfRooms(),
                    payload.getAmount());

            StandardResponse<?> response = channexWebhookService.processBookingWebhook(payload);
            log.info("Webhook result: success={}, message={}", response.isSuccess(), response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to process Channex webhook. Raw body: {}", rawBody, e);
            return ResponseEntity.status(500).body(
                    StandardResponse.error("Error: " + e.getMessage(), "CHANNEX_WEBHOOK_ERROR", e.toString())
            );
        }
    }
}
