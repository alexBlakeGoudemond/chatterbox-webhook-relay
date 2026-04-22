package com.webhook.relay.chatterbox.application.port.in.event.handler;

import com.webhook.relay.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import com.webhook.relay.chatterbox.application.domain.event.notification.WebhookEventProcessed;

public interface EventUpdateHandlerPort {

    void handle(PolledEventsProcessed polledEventsProcessed);

    void handle(WebhookEventProcessed webhookEventProcessed);

}
