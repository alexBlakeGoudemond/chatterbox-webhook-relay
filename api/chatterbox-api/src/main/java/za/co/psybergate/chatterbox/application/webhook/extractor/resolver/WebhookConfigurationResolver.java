package za.co.psybergate.chatterbox.application.webhook.extractor.resolver;

import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

public interface WebhookConfigurationResolver {

    ChatterboxConfigurationProperties.PayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    String getDestinationUrl(String repositoryName) throws InternalServerException;

}
