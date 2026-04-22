package com.webhook.relay.chatterbox.application.port.in.webhook.orchestration;

import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventReceived;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventReceived;

import java.time.LocalDateTime;
import java.util.List;

/// Orchestrates flow end-to-end:
/// `ingest → process → route → send downstream`
public interface WebhookOrchestratorPort {

    WebhookEventReceived process(String eventType, String deliveryId, String rawBody);

    List<WebhookPolledEventReceived> pollForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime);

    List<WebhookPolledEventReceived> pollForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<WebhookPolledEventReceived> pollForChanges(String repository, LocalDateTime receivedAt);

    boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName);

    List<String> getAllRepositories();

}
