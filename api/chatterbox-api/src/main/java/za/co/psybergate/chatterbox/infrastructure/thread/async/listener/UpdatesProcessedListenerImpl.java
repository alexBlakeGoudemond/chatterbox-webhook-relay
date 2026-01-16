package za.co.psybergate.chatterbox.infrastructure.thread.async.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.event.processor.EventProcessorService;
import za.co.psybergate.chatterbox.application.thread.async.listener.UpdatesProcessedListener;
import za.co.psybergate.chatterbox.domain.event.PolledEventsProcessed;
import za.co.psybergate.chatterbox.domain.event.WebhookEventProcessed;

// TODO BlakeGoudemond 2026/01/04 | retry cron job?
@Component
@RequiredArgsConstructor
public class UpdatesProcessedListenerImpl implements UpdatesProcessedListener {

    private final EventProcessorService eventProcessorService;

    private final WebhookLogger webhookLogger;

    @Async("polledEventExecutor")
    @EventListener
    @Override
    public void onPolledEventsProcessed(PolledEventsProcessed polledEventsProcessed) {
        webhookLogger.logPolledEventProcessed(polledEventsProcessed);
        eventProcessorService.processPolledEvents();
    }

    @Async("webhookEventExecutor")
    @EventListener
    @Override
    public void onWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed) {
        webhookLogger.logWebhookEventProcessed(webhookEventProcessed);
        eventProcessorService.processWebhookEvents();
    }

}
