package za.co.psybergate.chatterbox.application.async.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.processor.EventProcessorService;
import za.co.psybergate.chatterbox.domain.event.PolledEventsProcessed;
import za.co.psybergate.chatterbox.domain.event.WebhookEventProcessed;

// TODO BlakeGoudemond 2026/01/04 | retry cron job?
@Component
@RequiredArgsConstructor
public class UpdatesProcessedListener {

    private final EventProcessorService eventProcessorService;

    private final WebhookLogger webhookLogger;

    @Async("polledEventExecutor")
    @EventListener
    public void onPolledEventsProcessed(PolledEventsProcessed polledEventsProcessed) {
        webhookLogger.logPolledEventProcessed(polledEventsProcessed);
        eventProcessorService.processPolledEvents();
    }

    @Async("webhookEventExecutor")
    @EventListener
    public void onWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed) {
        webhookLogger.logWebhookEventProcessed(webhookEventProcessed);
        eventProcessorService.processWebhookEvents();
    }

}
