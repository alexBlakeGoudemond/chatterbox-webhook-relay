package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.record.WebhookEventDeliveryRecord;
import za.co.psybergate.chatterbox.domain.record.WebhookEventRecord;

import java.util.List;

public interface WebhookReceivedStore {

    List<WebhookEventRecord> getLatestProcessedWebhooks(String repositoryFullName);

    List<WebhookEventRecord> getUnprocessedWebhooks(String repositoryFullName);

    WebhookEventRecord storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    WebhookEventDeliveryRecord storeSuccessfulDelivery(WebhookEventRecord webhookEvent, String destinationName, String destinationUrl);

    WebhookEventDeliveryRecord storeUnsuccessfulDelivery(WebhookEventRecord webhookEvent, String destinationName, String destinationUrl);

    void setProcessedStatus(WebhookEventRecord webhookEvent, EventStatus eventStatus);

    WebhookEventRecord getWebhook(Long id);

    List<WebhookEventDeliveryRecord> getDeliveryLogs(Long id);

    WebhookEventRecord getMostRecentWebhook(String repositoryName);

}
