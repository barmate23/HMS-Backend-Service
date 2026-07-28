package com.hotelerp.frontoffice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hotelerp.frontoffice.common.StandardResponse;

public interface ChannexWebhookService {
    StandardResponse<?> processBookingWebhook(JsonNode payload);
}
