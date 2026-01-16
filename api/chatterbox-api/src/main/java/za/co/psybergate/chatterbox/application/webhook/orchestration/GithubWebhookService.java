package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.domain.persistence.dto.GithubPolledEventRecord;
import za.co.psybergate.chatterbox.domain.persistence.dto.WebhookEventRecord;

import java.time.LocalDateTime;
import java.util.List;

/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface GithubWebhookService {

    WebhookEventRecord process(String eventType, String deliveryId, JsonNode rawBody);

    List<GithubPolledEventRecord> pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime);

    List<GithubPolledEventRecord> pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<GithubPolledEventRecord> pollGithubForChanges(String repository, LocalDateTime receivedAt);

}
