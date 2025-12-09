package za.co.psybergate.chatterbox.application.webhook.validator;

import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

public interface WebhookRequestValidator {

    void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException;

}
