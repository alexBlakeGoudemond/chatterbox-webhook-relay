package za.co.psybergate.chatterbox.infrastructure.adapter.webhook.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.usecase.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.usecase.webhook.validation.WebhookRequestValidator;
import za.co.psybergate.chatterbox.infrastructure.common.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.infrastructure.common.config.properties.ChatterboxSourceGithubRepositoryProperties;

@Component
@RequiredArgsConstructor
public class WebhookRequestValidatorImpl implements WebhookRequestValidator {

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
