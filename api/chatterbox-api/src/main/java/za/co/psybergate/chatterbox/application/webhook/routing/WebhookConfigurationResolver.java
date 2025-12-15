package za.co.psybergate.chatterbox.application.webhook.routing;

import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolver {

    ChatterboxSourceGithubPayloadProperties.EventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    String getDestinationUrl(String repositoryName) throws UnrecognizedRequestException;

}
