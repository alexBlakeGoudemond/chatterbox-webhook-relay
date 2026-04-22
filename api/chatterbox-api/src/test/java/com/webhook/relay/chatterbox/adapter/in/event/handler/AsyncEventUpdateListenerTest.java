package com.webhook.relay.chatterbox.adapter.in.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.webhook.relay.chatterbox.application.common.logging.WebhookLogger;
import com.webhook.relay.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import com.webhook.relay.chatterbox.application.domain.event.notification.WebhookEventProcessed;
import com.webhook.relay.chatterbox.application.port.in.event.processor.EventProcessorPort;

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
        WebhookEventProcessed event = new WebhookEventProcessed("repositoryFullName");
        asyncEventUpdateListener.handle(event);
        verify(webhookLogger).logWebhookEventProcessed(event);
        verify(eventProcessor).processWebhookEvent(event.getRepositoryFullName());
    }

}
