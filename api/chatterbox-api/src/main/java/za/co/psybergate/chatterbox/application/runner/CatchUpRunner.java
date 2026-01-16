package za.co.psybergate.chatterbox.application.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledEventStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookEventStore;
import za.co.psybergate.chatterbox.application.webhook.orchestration.GithubWebhookService;
import za.co.psybergate.chatterbox.application.webhook.resolution.WebhookConfigurationResolver;
import za.co.psybergate.chatterbox.domain.event.PolledEventsProcessed;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatchUpRunner implements ApplicationRunner {

    private final GithubWebhookService webhookService;

    private final WebhookConfigurationResolver configurationResolver;

    private final WebhookEventStore webhookEventStore;

    private final GithubPolledEventStore githubPolledEventStore;

    private final WebhookLogger webhookLogger;

    private final ApplicationEventPublisher publisher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> repositories = configurationResolver.getAllRepositories();
        boolean webhookEventsFound = false;
        for (String repositoryFullName : repositories) {
            if (webhookService.findMostRecentWebhookAndCheckForUpdatesSince(repositoryFullName)) {
                webhookEventsFound = true;
            }
        }
        if (webhookEventsFound) {
            publisher.publishEvent(new PolledEventsProcessed());
        }
    }

}
