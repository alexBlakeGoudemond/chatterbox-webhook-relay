package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;

import java.time.LocalDateTime;

/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface GithubWebhookService {

    WebhookEvent process(String eventType, String deliveryId, JsonNode rawBody);

    void pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime);

    void pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

}
