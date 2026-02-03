package za.co.psybergate.chatterbox.application.common.thread.sync.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OnStartupCatchUpRunner implements CatchUpRunner, ApplicationRunner {

    private final WebhookOrchestratorPort webhookService;

    private final WebhookConfigurationResolverPort configurationResolver;

    private final ApplicationEventPublisher publisher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> repositories = getAllRepositories();
        processMissedEvents(repositories);
    }

    @Override
    public List<String> getAllRepositories() {
        return configurationResolver.getAllRepositories();
    }

    @Override
    public void processMissedEvents(List<String> repositories) {
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
