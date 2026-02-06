package za.co.psybergate.chatterbox.application.port.out.persistence;

import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventDelivery;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventReceived;

import java.util.List;

public interface WebhookPolledEventStorePort {

    List<WebhookPolledEventReceived> getUnprocessedEvents(String repositoryFullName);

    WebhookPolledEventReceived storeEvent(String uniqueId, OutboundEvent outboundEvent, RawEventPayload rawBody);

    List<WebhookPolledEventReceived> getLatestProcessedEvents(String repositoryFullName);

    WebhookPolledEventDelivery storeSuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl);

    WebhookPolledEventDelivery storeUnsuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl);

    void markProcessed(OutboundEvent outboundEvent, WebhookEventStatus webhookEventStatus);

    WebhookPolledEventReceived getEvent(Long id);

    List<WebhookPolledEventDelivery> getDeliveryLogs(Long id);

    WebhookPolledEventReceived getMostRecentPolledEvent(String repositoryFullName);

}
