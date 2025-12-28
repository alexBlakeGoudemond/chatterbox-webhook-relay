package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;

public interface WebhookReceivedStore {

    WebhookEvent storeWebhook(WebhookEvent webhook);

    WebhookEvent storeWebhook(GithubEventDto eventDto, JsonNode rawBody);

    boolean hasAlreadyBeenStored(String repositoryFullName, String webhookId);

    WebhookEvent getLatestWebhook(String repositoryFullName);

}
