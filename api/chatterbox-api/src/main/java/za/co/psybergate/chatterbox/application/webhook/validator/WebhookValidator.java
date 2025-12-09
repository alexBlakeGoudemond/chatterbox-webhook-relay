package za.co.psybergate.chatterbox.application.webhook.validator;

import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

public interface WebhookValidator {

    void assertAcceptedEvent(String eventType) throws UnrecognizedRequestException;

    void assertAcceptedRepository(String repositoryName) throws UnrecognizedRequestException;

    ChatterboxConfigurationProperties.PayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    String getDestinationUrl(String repositoryName) throws InternalServerException;

}
