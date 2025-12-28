package za.co.psybergate.chatterbox.application.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookReceived;

public interface WebhookReceivedStore {

    WebhookReceived storeWebhook(WebhookReceived webhook);

    boolean hasAlreadyBeenStored(String webhookId);

    WebhookReceived getLatestWebhook();

    void storeWebhook(GithubEventDto eventDto, JsonNode rawBody);

}
