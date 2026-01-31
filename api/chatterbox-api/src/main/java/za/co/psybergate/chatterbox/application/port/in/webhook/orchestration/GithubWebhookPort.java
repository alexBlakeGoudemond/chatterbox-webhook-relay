package za.co.psybergate.chatterbox.application.port.in.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubPolledEventDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;

import java.time.LocalDateTime;
import java.util.List;

/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface GithubWebhookPort {

    WebhookEventReceivedDto process(String eventType, String deliveryId, JsonNode rawBody);

    List<GithubPolledEventDto> pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime);

    List<GithubPolledEventDto> pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<GithubPolledEventDto> pollGithubForChanges(String repository, LocalDateTime receivedAt);

    boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName);

}
