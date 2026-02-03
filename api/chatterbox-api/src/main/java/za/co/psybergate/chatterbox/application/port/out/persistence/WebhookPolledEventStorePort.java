package za.co.psybergate.chatterbox.application.port.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;

import java.util.List;

public interface WebhookPolledEventStorePort {

    List<WebhookPolledEventReceivedDto> getUnprocessedEvents(String repositoryFullName);

    WebhookPolledEventReceivedDto storeEvent(String uniqueId, OutboundEvent outboundEvent, JsonNode rawBody);

    List<WebhookPolledEventReceivedDto> getLatestProcessedEvents(String repositoryFullName);

    WebhookPolledEventDeliveryDto storeSuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl);

    WebhookPolledEventDeliveryDto storeUnsuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl);

    void markProcessed(OutboundEvent outboundEvent, WebhookEventStatus webhookEventStatus);

    WebhookPolledEventReceivedDto getEvent(Long id);

    List<WebhookPolledEventDeliveryDto> getDeliveryLogs(Long id);

    WebhookPolledEventReceivedDto getMostRecentPolledEvent(String repositoryFullName);

}
