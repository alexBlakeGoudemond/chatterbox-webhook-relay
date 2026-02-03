package za.co.psybergate.chatterbox.application.port.out.persistence;

import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;

import java.util.List;

public interface WebhookEventStorePort {

    List<WebhookEventReceivedDto> getLatestProcessedWebhooks(String repositoryFullName);

    List<WebhookEventReceivedDto> getUnprocessedWebhooks(String repositoryFullName);

    WebhookEventReceivedDto storeWebhook(String uniqueId, OutboundEvent outboundEvent, RawEventPayload rawBody);

    WebhookEventDeliveryDto storeSuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl);

    WebhookEventDeliveryDto storeUnsuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl);

    void markProcessed(OutboundEvent outboundEvent, WebhookEventStatus webhookEventStatus);

    WebhookEventReceivedDto getWebhook(Long id);

    List<WebhookEventDeliveryDto> getDeliveryLogs(Long id);

    WebhookEventReceivedDto getMostRecentWebhook(String repositoryName);

}
