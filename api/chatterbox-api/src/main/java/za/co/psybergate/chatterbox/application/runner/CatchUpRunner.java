package za.co.psybergate.chatterbox.application.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.webhook.orchestration.GithubWebhookService;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolver;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatchUpRunner implements ApplicationRunner {

    private final GithubWebhookService webhookService;

    private final WebhookConfigurationResolver configurationResolver;

    private final WebhookReceivedStore webhookReceivedStore;

    private final WebhookLogger webhookLogger;

    private final ApplicationEventPublisher publisher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> repositories = configurationResolver.getAllRepositories();
        for (String repositoryFullName : repositories) {
            findMostRecentWebhookAndCheckForUpdatesSince(repositoryFullName);
        }
        // 4. After each new event saved, publisher.publishEvent(new EventSaved(id));
    }

    private void findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName) {
        try {
            WebhookEvent webhookEvent = webhookReceivedStore.getMostRecentWebhook(repositoryFullName);
            webhookService.pollGithubForChanges(repositoryFullName, webhookEvent.getReceivedAt());
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousWebhooks(repositoryFullName);
        }
    }

}
