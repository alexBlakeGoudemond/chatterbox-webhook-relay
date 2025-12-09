package za.co.psybergate.chatterbox.application.webhook.routing;

import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolver {

    ChatterboxConfigurationProperties.PayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    String getDestinationUrl(String repositoryName) throws InternalServerException;

}
