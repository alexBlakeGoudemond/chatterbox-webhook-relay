package za.co.psybergate.chatterbox.application.port.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;

import java.util.List;

public interface GithubPolledEventStorePort {

    List<WebhookPolledEventReceivedDto> getUnprocessedEvents(String repositoryFullName);

    WebhookPolledEventReceivedDto storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    List<WebhookPolledEventReceivedDto> getLatestProcessedEvents(String repositoryFullName);

    WebhookPolledEventDeliveryDto storeSuccessfulDelivery(WebhookPolledEventReceivedDto polledEvent, String destinationName, String destinationUrl);

    WebhookPolledEventDeliveryDto storeUnsuccessfulDelivery(WebhookPolledEventReceivedDto polledEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(WebhookPolledEventReceivedDto polledEvent, WebhookEventStatus webhookEventStatus);

    WebhookPolledEventReceivedDto getEvent(Long id);

    List<WebhookPolledEventDeliveryDto> getDeliveryLogs(Long id);

    WebhookPolledEventReceivedDto getMostRecentPolledEvent(String repositoryFullName);

}
