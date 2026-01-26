package za.co.psybergate.chatterbox.application.port.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.event.model.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.domain.event.model.WebhookEventDto;

import java.util.List;

public interface WebhookEventStorePort {

    List<WebhookEventDto> getLatestProcessedWebhooks(String repositoryFullName);

    List<WebhookEventDto> getUnprocessedWebhooks(String repositoryFullName);

    WebhookEventDto storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    WebhookEventDeliveryDto storeSuccessfulDelivery(WebhookEventDto webhookEvent, String destinationName, String destinationUrl);

    WebhookEventDeliveryDto storeUnsuccessfulDelivery(WebhookEventDto webhookEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(WebhookEventDto webhookEvent, EventStatus eventStatus);

    WebhookEventDto getWebhook(Long id);

    List<WebhookEventDeliveryDto> getDeliveryLogs(Long id);

    WebhookEventDto getMostRecentWebhook(String repositoryName);

}
