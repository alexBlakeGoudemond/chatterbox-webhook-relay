package com.webhook.relay.chatterbox.application.common.logging.detail;

import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventReceived;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventReceived;

import java.time.LocalDateTime;
import java.util.List;

public interface OrchestrationLogger {

    void logRunnerFoundNoPreviousWebhooks(String repositoryFullName);

    void logRunnerFoundPreviousWebhook(WebhookEventReceived latestWebhookEvent);

    void logRunnerFoundNoPreviousPolledEvents(String repositoryFullName);

    void logRunnerFoundPreviousPolledEvent(WebhookPolledEventReceived latestGithubPolledEvent);

    void logNoPolledEventsFound(String repositoryFullName, LocalDateTime lastPersistedTime);

    void logPolledEventsFound(List<WebhookPolledEventReceived> githubPolledEvents, String repositoryFullName, LocalDateTime lastPersistedTime);

}
