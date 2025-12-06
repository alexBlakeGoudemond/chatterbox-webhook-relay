package za.co.psybergate.chatterbox.application.webhook.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookValidatorImpl implements WebhookValidator {

    private final ChatterboxConfigurationProperties configurationProperties;

    @Override
    public void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException {
        if (configurationProperties.containsEvent(eventType)) {
            return;
        }
        log.debug("No ConfigurationProperties Found for eventType: {}", eventType);
        String responseContent =
                String.format("Webhook received; no work done; unrecognized event '%s'", eventType);
        throw new UnrecognizedRequestException(responseContent);
    }

    @Override
    public void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException {
        if (configurationProperties.acceptsRepository(repositoryName)) {
            return;
        }
        log.debug("Repository '{}' is not whitelisted as an accepted repository", repositoryName);
        String responseContent =
                String.format("Webhook received; no work done; unrecognized repository '%s'", repositoryName);
        throw new UnrecognizedRequestException(responseContent);
    }

    @Override
    public ChatterboxConfigurationProperties.PayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException {
        var payloadMapping = configurationProperties.getGithubIncomingMappings().get(eventType);
        if (payloadMapping == null) {
            throw new UnrecognizedRequestException(String.format("Unsupported event type '%s'", eventType));
        }
        return payloadMapping;
    }

}
