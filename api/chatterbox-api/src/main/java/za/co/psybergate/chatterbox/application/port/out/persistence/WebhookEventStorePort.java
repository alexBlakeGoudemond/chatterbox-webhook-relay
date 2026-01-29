package za.co.psybergate.chatterbox.application.port.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventDto;

import java.util.List;

public interface WebhookEventStorePort {

    List<WebhookEventDto> getLatestProcessedWebhooks(String repositoryFullName);

    List<WebhookEventDto> getUnprocessedWebhooks(String repositoryFullName);

    WebhookEventDto storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    WebhookEventDeliveryDto storeSuccessfulDelivery(WebhookEventDto webhookEvent, String destinationName, String destinationUrl);

    WebhookEventDeliveryDto storeUnsuccessfulDelivery(WebhookEventDto webhookEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(WebhookEventDto webhookEvent, WebhookEventStatus webhookEventStatus);

    WebhookEventDto getWebhook(Long id);

    List<WebhookEventDeliveryDto> getDeliveryLogs(Long id);

    WebhookEventDto getMostRecentWebhook(String repositoryName);

}
