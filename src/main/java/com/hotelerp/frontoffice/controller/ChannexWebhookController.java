package com.hotelerp.frontoffice.controller;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.constants.ServiceConstants;
import com.hotelerp.frontoffice.dto.channex.ChannexWebhookPayload;
import com.hotelerp.frontoffice.service.ChannexWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to receive push webhooks from Channex Channel Manager.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ChannexWebhookController {

    private final ChannexWebhookService channexWebhookService;

    @PostMapping(ServiceConstants.RESERVATION_BASE_URL + ServiceConstants.CHANNEX_WEBHOOK)
    public ResponseEntity<StandardResponse<?>> handleChannexBookingWebhook(
            @RequestBody ChannexWebhookPayload payload) {
        log.info("Received POST webhook request from Channex Channel Manager");
        StandardResponse<?> response = channexWebhookService.processBookingWebhook(payload);
        return ResponseEntity.ok(response);
    }
}
