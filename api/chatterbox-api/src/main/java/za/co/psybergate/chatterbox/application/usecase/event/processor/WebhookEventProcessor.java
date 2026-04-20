package com.webhook.relay.chatterbox.application.usecase.event.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.webhook.relay.chatterbox.application.common.logging.WebhookLogger;
import com.webhook.relay.chatterbox.application.common.map.ApplicationMapper;
import com.webhook.relay.chatterbox.application.domain.configuration.DestinationMapping;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventStatus;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventReceived;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventReceived;
import com.webhook.relay.chatterbox.application.port.in.event.processor.EventProcessorPort;
import com.webhook.relay.chatterbox.application.port.out.delivery.EventDeliveryPort;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import com.webhook.relay.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

@Service
@RequiredArgsConstructor
@Transactional
public class WebhookEventProcessor implements EventProcessorPort {

    private final WebhookLogger webhookLogger;

    private final WebhookConfigurationResolverPort configurationResolver;

    private final WebhookPolledEventStorePort polledEventStore;

    private final WebhookEventStorePort webhookEventStore;

    private final EventDeliveryPort eventDelivery;

    @Override
    public void processWebhookEvent(String repositoryFullName) {
        for (DestinationMapping mapping : configurationResolver.getDestinationMapping()) {
            if (!mapping.source().equalsIgnoreCase(repositoryFullName)){
                continue;
            }
            webhookLogger.logProcessingEvents(mapping);
            processWebhookEvents(mapping);
        }
    }

    @Override
    public void processPolledEvents() {
        for (DestinationMapping mapping : configurationResolver.getDestinationMapping()) {
            webhookLogger.logProcessingEvents(mapping);
            processPolledEvents(mapping);
        }
    }

    private void processWebhookEvents(DestinationMapping mapping) {
        for (WebhookEventReceived event : webhookEventStore.getUnprocessedWebhooks(mapping.source())) {
            OutboundEvent outbound = ApplicationMapper.mapToOutboundEvent(event);
            eventDelivery.deliver(outbound, mapping);
            webhookEventStore.markProcessed(outbound, WebhookEventStatus.PROCESSED_SUCCESS);
        }
    }

    private void processPolledEvents(DestinationMapping mapping) {
        for (WebhookPolledEventReceived event : polledEventStore.getUnprocessedEvents(mapping.source())) {
            OutboundEvent outbound = ApplicationMapper.mapToOutboundEvent(event);
            eventDelivery.deliver(outbound, mapping);
            polledEventStore.markProcessed(outbound, WebhookEventStatus.PROCESSED_SUCCESS);
        }
    }

}
