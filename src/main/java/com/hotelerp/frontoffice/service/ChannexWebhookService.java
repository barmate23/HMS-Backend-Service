package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.channex.ChannexWebhookPayload;

public interface ChannexWebhookService {
    StandardResponse<?> processBookingWebhook(ChannexWebhookPayload payload);
}
