package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface GithubWebhookService {

    void process(String eventType, String deliveryId, JsonNode rawBody);

    void pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime);

    void pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    void deliverToTeams(String eventType, String deliveryId, JsonNode rawBody);
}
