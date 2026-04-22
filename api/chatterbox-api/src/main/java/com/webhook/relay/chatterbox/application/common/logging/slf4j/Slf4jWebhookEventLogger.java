package com.webhook.relay.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.common.logging.detail.WebhookEventLogger;

@Slf4j
@Component
public class Slf4jWebhookEventLogger implements WebhookEventLogger {

    @Override
    public void logReceivedWebhookEvent(String event, String delivery) {
        log.info("[Webhook] Received event={} delivery={}", event, delivery);
    }

    @Override
    public void logCompletion(long ms) {
        log.debug("[Webhook] Completed in {}ms", ms);
    }

}
