package za.co.psybergate.chatterbox.application.port.in.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;

import java.time.LocalDateTime;
import java.util.List;

// TODO BlakeGoudemond 2026/02/03 | should not mention Github
/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface GithubWebhookPort {

    WebhookEventReceivedDto process(String eventType, String deliveryId, JsonNode rawBody);

    List<WebhookPolledEventReceivedDto> pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime);

    List<WebhookPolledEventReceivedDto> pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<WebhookPolledEventReceivedDto> pollGithubForChanges(String repository, LocalDateTime receivedAt);

    boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName);

}
