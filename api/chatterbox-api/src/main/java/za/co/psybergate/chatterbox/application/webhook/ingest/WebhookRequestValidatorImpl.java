package za.co.psybergate.chatterbox.application.webhook.ingest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

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

}
