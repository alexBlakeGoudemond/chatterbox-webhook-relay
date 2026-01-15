package za.co.psybergate.chatterbox.application.aysnc.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.processor.EventProcessor;
import za.co.psybergate.chatterbox.infrastructure.event.PolledEventsProcessed;
import za.co.psybergate.chatterbox.infrastructure.event.WebhookEventProcessed;

// TODO BlakeGoudemond 2026/01/04 | retry cron job?
@Component
@RequiredArgsConstructor
public class UpdatesProcessedListener {

    private final EventProcessor eventProcessor;

    private final WebhookLogger webhookLogger;

    @Async("polledEventExecutor")
    @EventListener
    public void onPolledEventsProcessed(PolledEventsProcessed polledEventsProcessed){
        webhookLogger.logPolledEventProcessed(polledEventsProcessed);
        eventProcessor.processPolledEvents();
    }

    @Async("webhookEventExecutor")
    @EventListener
    public void onWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed) {
        webhookLogger.logWebhookEventProcessed(webhookEventProcessed);
        eventProcessor.processWebhookEvents();
    }

}
