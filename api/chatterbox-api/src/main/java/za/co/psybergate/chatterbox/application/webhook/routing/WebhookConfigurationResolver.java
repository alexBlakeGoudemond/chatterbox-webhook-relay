package za.co.psybergate.chatterbox.application.webhook.routing;

import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;

import java.util.List;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolver {

    ChatterboxSourceGithubPayloadProperties.EventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    ChatterboxSourceGithubPayloadProperties.EventMapping getPayloadMapping(EventType eventType) throws UnrecognizedRequestException;

    String getTeamsDestinationUrl(String repositoryName) throws UnrecognizedRequestException;

    List<String> getAllRepositories();

}
