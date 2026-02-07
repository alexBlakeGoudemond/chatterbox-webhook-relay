package za.co.psybergate.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.detail.PollingLogger;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventReceived;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventReceived;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class Slf4jPollingLogger extends AbstractSlf4jLogger implements PollingLogger {

    @Override
    public void logPollRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        log.info("[GithubAPI] querying '{}/{}' for any updates since {} - {}", owner, repositoryName, fromDate, untilDate);
    }

    @Override
    public void logPollEventType(String eventType, String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        log.debug("[GithubAPI] querying if any {} occurred for '{}/{}' since {} - {}", eventType, owner, repositoryName, fromDate, untilDate);
    }

    @Override
    public void logRunnerFoundNoPreviousWebhooks(String repositoryFullName) {
        log.warn("[Runner] No previous webhooks found for the destination '{}', will not Poll", repositoryFullName);
    }

    @Override
    public void logRunnerFoundPreviousWebhook(WebhookEventReceived latestWebhookEvent) {
        log.info("[Runner] Previous webhook found '{}', continuing with Poll", truncate(latestWebhookEvent));
    }

    @Override
    public void logRunnerFoundNoPreviousPolledEvents(String repositoryFullName) {
        log.warn("[Runner] No previous polled events found for the destination '{}', not participating in Poll", repositoryFullName);
    }

    @Override
    public void logRunnerFoundPreviousPolledEvent(WebhookPolledEventReceived latestGithubPolledEvent) {
        log.info("[Runner] Previous polled event found '{}', continuing with Poll", truncate(latestGithubPolledEvent));
    }

    @Override
    public void logPolledEventsEmpty(String repositoryFullName) {
        log.warn("[Polling] No GithubPolledEvents found for the destination '{}'", repositoryFullName);
    }

    @Override
    public void logWebhookEventsEmpty(String repositoryFullName) {
        log.warn("[Processing] No WebhookEvents found for the destination '{}'", repositoryFullName);
    }

    @Override
    public void logNoPolledEventsFound(String repositoryFullName, LocalDateTime lastPersistedTime) {
        log.warn("[Polling] No GithubPolledEvents found for '{}' since '{}'", repositoryFullName, lastPersistedTime);
    }

    @Override
    public void logPolledEventsFound(List<WebhookPolledEventReceived> githubPolledEvents, String repositoryFullName, LocalDateTime lastPersistedTime) {
        log.info("[Polling] Found {} GithubPolledEvents for '{}' since '{}'", githubPolledEvents.size(), repositoryFullName, lastPersistedTime);
    }
}
