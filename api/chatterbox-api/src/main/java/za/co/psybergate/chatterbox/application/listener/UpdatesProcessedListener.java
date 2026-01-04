package za.co.psybergate.chatterbox.application.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.processor.EventProcessor;
import za.co.psybergate.chatterbox.infrastructure.event.PolledEventsProcessed;

@Component
@RequiredArgsConstructor
public class UpdatesProcessedListener {

    private final EventProcessor eventProcessor;

    @Async
    @EventListener
    public void onUpdatesProcessed(PolledEventsProcessed polledEventsProcessed){
        eventProcessor.processPolledEvents();
    }

}
