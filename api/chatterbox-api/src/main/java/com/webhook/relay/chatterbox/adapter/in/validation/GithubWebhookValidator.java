package com.webhook.relay.chatterbox.adapter.in.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.common.exception.UnrecognizedRequestException;
import com.webhook.relay.chatterbox.application.common.logging.WebhookLogger;
import com.webhook.relay.chatterbox.application.port.in.validation.WebhookRequestValidatorPort;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxSourceGithubPayloadProperties;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxSourceGithubRepositoryProperties;

@Component
@RequiredArgsConstructor
public class GithubWebhookValidator implements WebhookRequestValidatorPort {

    private final ChatterboxSourceGithubPayloadProperties payloadProperties;

    private final ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    private final WebhookLogger webhookLogger;

    @Override
    public void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException {
        if (payloadProperties.containsEvent(eventType)) {
            return;
        }
        webhookLogger.logUnknownEventType(eventType);
        String responseContent =
                String.format("Webhook received; no work done; unrecognized event '%s'", eventType);
        throw new UnrecognizedRequestException(responseContent);
    }

    @Override
    public void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException {
        if (repositoryProperties.acceptsRepository(repositoryName)) {
            return;
        }
        webhookLogger.logUnrecognizedRepository(repositoryName);
        String responseContent =
                String.format("Webhook received; no work done; unrecognized repository '%s'", repositoryName);
        throw new UnrecognizedRequestException(responseContent);
    }

    @Override
    public void assertAcceptedRepository(String owner, String repositoryName) throws UnrecognizedRequestException {
        String repositoryFullName = String.format("%s/%s", owner, repositoryName);
        assertAcceptedRepository(repositoryFullName);
    }

}
