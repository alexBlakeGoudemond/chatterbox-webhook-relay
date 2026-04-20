package com.webhook.relay.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.common.logging.detail.ProcessingLogger;
import com.webhook.relay.chatterbox.application.domain.configuration.DestinationMapping;
import com.webhook.relay.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import com.webhook.relay.chatterbox.application.domain.event.notification.WebhookEventProcessed;

@Slf4j
@Component
public class Slf4jProcessingLogger implements ProcessingLogger {

    @Override
    public void logProcessingEvents(DestinationMapping destinationMapping) {
        log.info("[Processing] Processing Received Webhook events for destination: '{}'", destinationMapping.source());
    }

    @Override
    public void logPolledEventProcessed(PolledEventsProcessed polledEventsProcessed) {
        log.debug("[Listener] PolledEventsProcessed have occurred: {}", polledEventsProcessed);
    }

    @Override
    public void logWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed) {
        log.debug("[Listener] WebhookEventProcessed has occurred: {}", webhookEventProcessed);
    }

}
