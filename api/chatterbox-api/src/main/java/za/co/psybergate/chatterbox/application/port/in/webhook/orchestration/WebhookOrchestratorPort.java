package za.co.psybergate.chatterbox.application.port.in.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceived;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceived;

import java.time.LocalDateTime;
import java.util.List;

/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface WebhookOrchestratorPort {

    WebhookEventReceived process(String eventType, String deliveryId, JsonNode rawBody);

    List<WebhookPolledEventReceived> pollForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime);

    List<WebhookPolledEventReceived> pollForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<WebhookPolledEventReceived> pollForChanges(String repository, LocalDateTime receivedAt);

    boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName);

}
