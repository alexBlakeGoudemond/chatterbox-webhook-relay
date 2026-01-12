package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventDeliveryLog;

import java.util.List;

public interface WebhookReceivedStore {

    boolean hasAlreadyBeenStored(String repositoryFullName, String webhookId);

    List<WebhookEvent> getLatestProcessedWebhooks(String repositoryFullName);

    List<WebhookEvent> getUnprocessedWebhooks(String repositoryFullName);

    WebhookEvent storeWebhook(WebhookEvent webhook);

    WebhookEvent storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    WebhookEventDeliveryLog storeSuccessfulDelivery(WebhookEventDeliveryLog webhookEventDeliveryLog);

    WebhookEventDeliveryLog storeSuccessfulDelivery(WebhookEvent webhookEvent, String destinationName, String destinationUrl);

    WebhookEventDeliveryLog storeUnsuccessfulDelivery(WebhookEvent webhookEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(WebhookEvent webhookEvent, EventStatus eventStatus);

    void setProcessedStatus(WebhookEvent webhookEvent, EventStatus eventStatus, String responseDtoErrorResponse);

    WebhookEvent getWebhook(Long id);

    List<WebhookEventDeliveryLog> getDeliveryLogs(Long id);

    WebhookEvent getMostRecentWebhook(String repositoryName);

}
