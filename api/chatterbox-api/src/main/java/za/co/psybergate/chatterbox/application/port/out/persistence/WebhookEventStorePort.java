package za.co.psybergate.chatterbox.application.port.out.persistence;

import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventDelivery;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventReceived;

import java.util.List;

public interface WebhookEventStorePort {

    List<WebhookEventReceived> getLatestProcessedWebhooks(String repositoryFullName);

    List<WebhookEventReceived> getUnprocessedWebhooks(String repositoryFullName);

    WebhookEventReceived storeWebhook(String uniqueId, OutboundEvent outboundEvent, RawEventPayload rawBody);

    WebhookEventDelivery storeSuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl);

    WebhookEventDelivery storeUnsuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl);

    void markProcessed(OutboundEvent outboundEvent, WebhookEventStatus webhookEventStatus);

    WebhookEventReceived getWebhook(Long id);

    List<WebhookEventDelivery> getDeliveryLogs(Long id);

    WebhookEventReceived getMostRecentWebhook(String repositoryName);

}
