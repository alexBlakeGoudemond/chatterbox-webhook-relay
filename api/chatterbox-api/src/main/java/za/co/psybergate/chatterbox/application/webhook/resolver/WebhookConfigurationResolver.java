package za.co.psybergate.chatterbox.application.webhook.resolver;

import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

public interface WebhookConfigurationResolver {

    ChatterboxConfigurationProperties.PayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    // TODO BlakeGoudemond 2025/12/09 | move to another component
    String getDestinationUrl(String repositoryName) throws InternalServerException;

}
