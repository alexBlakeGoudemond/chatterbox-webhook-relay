package za.co.psybergate.chatterbox.adapter.in.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;
import za.co.psybergate.chatterbox.application.port.in.event.processor.EventProcessorPort;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AsyncEventUpdateListenerTest {

    @Mock
    private EventProcessorPort eventProcessor;

    @Mock
    private WebhookLogger webhookLogger;

    private AsyncEventUpdateListener asyncEventUpdateListener;

    @BeforeEach
    void setUp() {
        asyncEventUpdateListener = new AsyncEventUpdateListener(eventProcessor, webhookLogger);
    }

    @Test
    @DisplayName("Should log and process polled events when handle(PolledEventsProcessed) is called")
    void givenPolledEventsProcessed_WhenHandleEvent_ThenEventProcessorIsCalled() {
        PolledEventsProcessed event = new PolledEventsProcessed();
        asyncEventUpdateListener.handle(event);
        verify(webhookLogger).logPolledEventProcessed(event);
        verify(eventProcessor).processPolledEvents();
    }

    @Test
    @DisplayName("Should log and process webhook events when handle(WebhookEventProcessed) is called")
    void givenWebhookEventProcessed_WhenHandleEvent_ThenEventProcessorIsCalled() {
        WebhookEventProcessed event = new WebhookEventProcessed();
        asyncEventUpdateListener.handle(event);
        verify(webhookLogger).logWebhookEventProcessed(event);
        verify(eventProcessor).processWebhookEvents();
    }

}
