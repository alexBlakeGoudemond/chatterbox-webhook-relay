package za.co.psybergate.chatterbox.adapter.in.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.MdcContext;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;
import za.co.psybergate.chatterbox.application.port.in.event.handler.EventUpdateHandlerPort;
import za.co.psybergate.chatterbox.application.port.in.event.processor.EventProcessorPort;

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
        MdcContext.setThreadExecutionId(polledEventsProcessed.getWebhookTrackingUuid());
        webhookLogger.logPolledEventProcessed(polledEventsProcessed);
        eventProcessor.processPolledEvents();
        MdcContext.clear();
    }

    @Async("webhookEventExecutor")
    @EventListener
    @Override
    public void handle(WebhookEventProcessed webhookEventProcessed) {
        MdcContext.setThreadExecutionId(webhookEventProcessed.getWebhookTrackingUuid());
        MdcContext.setRepositoryName(webhookEventProcessed.getRepositoryFullName());
        webhookLogger.logWebhookEventProcessed(webhookEventProcessed);
        eventProcessor.processWebhookEvent(webhookEventProcessed.getRepositoryFullName());
        MdcContext.clear();
    }

}
