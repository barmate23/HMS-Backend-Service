package com.hotelerp.frontoffice.service;

import com.hotelerp.frontoffice.common.StandardResponse;
import com.hotelerp.frontoffice.dto.channex.ChannexWebhookPayload;

public interface ChannexWebhookService {

    /**
     * Process incoming booking revision webhook from Channex.
     */
    StandardResponse<?> processBookingWebhook(ChannexWebhookPayload payload);
}
