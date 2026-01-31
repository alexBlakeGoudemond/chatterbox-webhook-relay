package za.co.psybergate.chatterbox.application.port.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;

import java.util.List;

public interface WebhookEventStorePort {

    List<WebhookEventReceivedDto> getLatestProcessedWebhooks(String repositoryFullName);

    List<WebhookEventReceivedDto> getUnprocessedWebhooks(String repositoryFullName);

    WebhookEventReceivedDto storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    WebhookEventDeliveryDto storeSuccessfulDelivery(WebhookEventReceivedDto webhookEvent, String destinationName, String destinationUrl);

    WebhookEventDeliveryDto storeUnsuccessfulDelivery(WebhookEventReceivedDto webhookEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(WebhookEventReceivedDto webhookEvent, WebhookEventStatus webhookEventStatus);

    WebhookEventReceivedDto getWebhook(Long id);

    List<WebhookEventDeliveryDto> getDeliveryLogs(Long id);

    WebhookEventReceivedDto getMostRecentWebhook(String repositoryName);

}
