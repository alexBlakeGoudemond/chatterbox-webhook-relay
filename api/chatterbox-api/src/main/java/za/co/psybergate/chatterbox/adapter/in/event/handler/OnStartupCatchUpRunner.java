package za.co.psybergate.chatterbox.adapter.in.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.MdcContext;
import za.co.psybergate.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import za.co.psybergate.chatterbox.application.port.in.event.handler.CatchUpHandlerPort;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OnStartupCatchUpRunner implements CatchUpHandlerPort, ApplicationRunner {

    private final WebhookOrchestratorPort webhookService;

    private final ApplicationEventPublisher publisher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> repositories = getAllRepositories();
        processMissedEvents(repositories);
    }

    @Override
    public List<String> getAllRepositories() {
        return webhookService.getAllRepositories();
    }

    @Override
    public void processMissedEvents(List<String> repositories) {
        boolean webhookEventsFound = false;
        for (String repositoryFullName : repositories) {
            MdcContext.setRepositoryName(repositoryFullName);
            if (webhookService.findMostRecentWebhookAndCheckForUpdatesSince(repositoryFullName)) {
                webhookEventsFound = true;
            }
        }
        if (webhookEventsFound) {
            publisher.publishEvent(new PolledEventsProcessed());
        }
    }

}
