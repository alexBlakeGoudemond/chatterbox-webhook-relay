package za.co.psybergate.chatterbox.application.port.in.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;

import java.time.LocalDateTime;
import java.util.List;

/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface WebhookOrchestratorPort {

    WebhookEventReceivedDto process(String eventType, String deliveryId, JsonNode rawBody);

    List<WebhookPolledEventReceivedDto> pollForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime);

    List<WebhookPolledEventReceivedDto> pollForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<WebhookPolledEventReceivedDto> pollForChanges(String repository, LocalDateTime receivedAt);

    boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName);

}
