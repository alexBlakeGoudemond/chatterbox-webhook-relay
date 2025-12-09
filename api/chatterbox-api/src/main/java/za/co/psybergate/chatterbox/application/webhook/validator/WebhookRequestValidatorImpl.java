package za.co.psybergate.chatterbox.application.webhook.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

@Service
@RequiredArgsConstructor
public class WebhookRequestValidatorImpl implements WebhookRequestValidator {

    private final ChatterboxConfigurationProperties configurationProperties;

    private final WebhookLogger webhookLogger;

    @Override
    public void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException {
        if (configurationProperties.containsEvent(eventType)) {
            return;
        }
        webhookLogger.logUnknownEventType(eventType);
        String responseContent =
                String.format("Webhook received; no work done; unrecognized event '%s'", eventType);
        throw new UnrecognizedRequestException(responseContent);
    }

    @Override
    public void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException {
        if (configurationProperties.acceptsRepository(repositoryName)) {
            return;
        }
        webhookLogger.logUnrecognizedRepository(repositoryName);
        String responseContent =
                String.format("Webhook received; no work done; unrecognized repository '%s'", repositoryName);
        throw new UnrecognizedRequestException(responseContent);
    }

}
