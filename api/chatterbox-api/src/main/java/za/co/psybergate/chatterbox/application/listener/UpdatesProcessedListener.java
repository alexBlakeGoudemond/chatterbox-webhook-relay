package za.co.psybergate.chatterbox.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.processor.EventProcessor;
import za.co.psybergate.chatterbox.infrastructure.event.PolledEventsProcessed;
import za.co.psybergate.chatterbox.infrastructure.event.WebhookEventProcessed;

@Component
@RequiredArgsConstructor
public class UpdatesProcessedListener {

    private final EventProcessor eventProcessor;

    @Async("polledEventExecutor")
    @EventListener
    public void onPolledEventsProcessed(PolledEventsProcessed polledEventsProcessed){
        eventProcessor.processPolledEvents();
    }

    @Async("webhookEventExecutor")
    @EventListener
    public void onWebhookEventProcessed(WebhookEventProcessed webhookEventProcessed) {
        eventProcessor.processWebhookEvents();
    }

}
