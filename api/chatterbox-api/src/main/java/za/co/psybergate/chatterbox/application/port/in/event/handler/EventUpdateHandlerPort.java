package za.co.psybergate.chatterbox.application.port.in.event.handler;

import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;

public interface EventUpdateHandlerPort {

    void handle(PolledEventsProcessed polledEventsProcessed);

    void handle(WebhookEventProcessed webhookEventProcessed);

}
