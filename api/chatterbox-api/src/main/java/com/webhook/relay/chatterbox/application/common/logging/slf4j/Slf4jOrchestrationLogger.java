package com.webhook.relay.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.common.logging.detail.OrchestrationLogger;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventReceived;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventReceived;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class Slf4jOrchestrationLogger extends AbstractSlf4jLogger implements OrchestrationLogger {

    @Override
    public void logRunnerFoundNoPreviousWebhooks(String repositoryFullName) {
        log.warn("[Orchestration] No previous webhooks found for the destination '{}', will not Poll", repositoryFullName);
    }

    @Override
    public void logRunnerFoundPreviousWebhook(WebhookEventReceived latestWebhookEvent) {
        log.info("[Orchestration] Previous webhook found '{}', continuing with Poll", truncate(latestWebhookEvent));
    }

    @Override
    public void logRunnerFoundNoPreviousPolledEvents(String repositoryFullName) {
        log.warn("[Orchestration] No previous polled events found for the destination '{}', not participating in Poll", repositoryFullName);
    }

    @Override
    public void logRunnerFoundPreviousPolledEvent(WebhookPolledEventReceived latestPolledEvent) {
        log.info("[Orchestration] Previous polled event found '{}', continuing with Poll", truncate(latestPolledEvent));
    }

    @Override
    public void logNoPolledEventsFound(String repositoryFullName, LocalDateTime lastPersistedTime) {
        log.warn("[Orchestration] No PolledEvents found for '{}' since '{}'", repositoryFullName, lastPersistedTime);
    }

    @Override
    public void logPolledEventsFound(List<WebhookPolledEventReceived> polledEvents, String repositoryFullName, LocalDateTime lastPersistedTime) {
        log.info("[Orchestration] Found {} PolledEvents for '{}' since '{}'", polledEvents.size(), repositoryFullName, lastPersistedTime);
    }

}
