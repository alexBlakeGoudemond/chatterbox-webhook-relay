package com.webhook.relay.chatterbox.adapter.in.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.common.logging.WebhookLogger;
import com.webhook.relay.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import com.webhook.relay.chatterbox.application.domain.event.notification.WebhookEventProcessed;
import com.webhook.relay.chatterbox.application.port.in.event.handler.EventUpdateHandlerPort;
import com.webhook.relay.chatterbox.application.port.in.event.processor.EventProcessorPort;

// TODO BlakeGoudemond 2026/01/04 | retry cron job?
@Component
@RequiredArgsConstructor
public class AsyncEventUpdateListener implements EventUpdateHandlerPort {

    private final EventProcessorPort eventProcessor;

    private final WebhookLogger webhookLogger;

    @Async("polledEventExecutor")
    @EventListener
    @Override
    public void handle(PolledEventsProcessed polledEventsProcessed) {
            webhookLogger.logPolledEventProcessed(polledEventsProcessed);
            eventProcessor.processPolledEvents();
    }

    @Async("webhookEventExecutor")
    @EventListener
    @Override
    public void handle(WebhookEventProcessed webhookEventProcessed) {
            webhookLogger.logWebhookEventProcessed(webhookEventProcessed);
            eventProcessor.processWebhookEvent(webhookEventProcessed.getRepositoryFullName());
    }

}
