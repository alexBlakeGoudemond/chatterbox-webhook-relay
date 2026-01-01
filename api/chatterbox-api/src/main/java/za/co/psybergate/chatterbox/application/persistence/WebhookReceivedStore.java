package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventLog;

public interface WebhookReceivedStore {

    boolean hasAlreadyBeenStored(String repositoryFullName, String webhookId);

    WebhookEvent getLatestWebhook(String repositoryFullName);

    WebhookEvent storeWebhook(WebhookEvent webhook);

    WebhookEvent storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody);

    WebhookEventLog storeDelivery(WebhookEventLog webhookEventLog);

    WebhookEventLog storeDelivery(WebhookEvent webhookEvent, String destinationName, String destinationUrl);
}
