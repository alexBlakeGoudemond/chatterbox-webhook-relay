package com.webhook.relay.chatterbox.adapter.in.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.common.logging.MdcContext;
import com.webhook.relay.chatterbox.application.domain.event.notification.PolledEventsProcessed;
import com.webhook.relay.chatterbox.application.port.in.event.handler.CatchUpHandlerPort;
import com.webhook.relay.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OnStartupCatchUpRunner implements CatchUpHandlerPort, ApplicationRunner {

    private final WebhookOrchestratorPort webhookService;

    private final ApplicationEventPublisher publisher;

    private final MdcContext mdcContext;

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
            mdcContext.setRepositoryName(repositoryFullName);
            try {
                if (webhookService.findMostRecentWebhookAndCheckForUpdatesSince(repositoryFullName)) {
                    webhookEventsFound = true;
                }
            } catch (Exception e) {
                // We don't want to stop the runner if one repository fails to poll (e.g. 404 or bad token)
                // The exception is already logged by GlobalExceptionHandler if it reached there,
                // but here we are in a runner, so we should log it.
            }
        }
        if (webhookEventsFound) {
            publisher.publishEvent(new PolledEventsProcessed());
        }
    }

}
