package za.co.psybergate.chatterbox.application.common.thread.async.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.port.in.event.processor.EventProcessorPort;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;

// TODO BlakeGoudemond 2026/01/04 | retry cron job?
@Component
@RequiredArgsConstructor
public class AsyncEventUpdateListener implements UpdatesProcessedListener {

    private final EventProcessorPort eventProcessor;

    private final WebhookLogger webhookLogger;

    @Async("polledEventExecutor")
    @EventListener
    @Override
    public void onPolledEventsProcessed(PolledEventsProcessed polledEventsProcessed) {
        webhookLogger.logPolledEventProcessed(polledEventsProcessed);
        eventProcessor.processPolledEvents();
    }

    @Async("webhookEventExecutor")
    @EventListener
    @Override
    public void onWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed) {
        webhookLogger.logWebhookEventProcessed(webhookEventProcessed);
        eventProcessor.processWebhookEvents();
    }

}
