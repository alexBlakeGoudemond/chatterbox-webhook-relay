package com.webhook.relay.chatterbox.application.common.logging.detail;

import com.webhook.relay.chatterbox.application.domain.configuration.DestinationMapping;
import com.webhook.relay.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import com.webhook.relay.chatterbox.application.domain.event.notification.WebhookEventProcessed;

public interface ProcessingLogger {

    void logProcessingEvents(DestinationMapping destinationMapping);

    void logPolledEventProcessed(PolledEventsProcessed polledEventsProcessed);

    void logWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed);

}
