package za.co.psybergate.chatterbox.application.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.webhook.orchestration.GithubWebhookService;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolver;
import za.co.psybergate.chatterbox.domain.event.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.persistence.dto.GithubPolledEventDto;
import za.co.psybergate.chatterbox.application.persistence.dto.WebhookEventDto;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CatchUpRunner implements ApplicationRunner {

    private final GithubWebhookService webhookService;

    private final WebhookConfigurationResolver configurationResolver;

    private final WebhookReceivedStore webhookReceivedStore;

    private final GithubPolledStore githubPolledStore;

    private final WebhookLogger webhookLogger;

    private final ApplicationEventPublisher publisher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> repositories = configurationResolver.getAllRepositories();
        boolean webhookEventsFound = false;
        for (String repositoryFullName : repositories) {
            if (findMostRecentWebhookAndCheckForUpdatesSince(repositoryFullName)) {
                webhookEventsFound = true;
            }
        }
        if (webhookEventsFound) {
            publisher.publishEvent(new PolledEventsProcessed());
        }
    }

    private boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName) {
        LocalDateTime lastPersistedTime;
        try {
            WebhookEventDto latestWebhookEvent = webhookReceivedStore.getMostRecentWebhook(repositoryFullName);
            webhookLogger.logRunnerFoundPreviousWebhook(latestWebhookEvent);
            lastPersistedTime = latestWebhookEvent.receivedAt();
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousWebhooks(repositoryFullName);
            return false;
        }
        try {
            GithubPolledEventDto latestGithubPolledEvent = githubPolledStore.getMostRecentPolledEvent(repositoryFullName);
            webhookLogger.logRunnerFoundPreviousPolledEvent(latestGithubPolledEvent);
            lastPersistedTime = getLastPersistedTime(lastPersistedTime, latestGithubPolledEvent.fetchedAt());
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousPolledEvents(repositoryFullName);
        }
        List<GithubPolledEventDto> githubPolledEvents = webhookService.pollGithubForChanges(repositoryFullName, lastPersistedTime);
        if (!githubPolledEvents.isEmpty()) {
            webhookLogger.logPolledEventsFound(githubPolledEvents, repositoryFullName, lastPersistedTime);
            return true;
        }
        webhookLogger.logNoPolledEventsFound(repositoryFullName, lastPersistedTime);
        return false;
    }

    private LocalDateTime getLastPersistedTime(LocalDateTime persistedTime001, LocalDateTime persistedTime002) {
        if (persistedTime001.isAfter(persistedTime002)) {
            return persistedTime001;
        }
        return persistedTime002;
    }

}
