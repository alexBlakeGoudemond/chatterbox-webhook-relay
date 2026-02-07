package za.co.psybergate.chatterbox.application.common.logging.detail;

import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;

public interface ProcessingLogger {

    void logProcessingEvents(DestinationMapping destinationMapping);

    void logPolledEventProcessed(PolledEventsProcessed polledEventsProcessed);

    void logWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed);

}
