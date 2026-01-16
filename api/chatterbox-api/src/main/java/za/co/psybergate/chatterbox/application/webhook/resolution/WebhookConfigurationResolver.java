package za.co.psybergate.chatterbox.application.webhook.resolution;

import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.github.GithubEventMapping;

import java.util.List;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolver {

    GithubEventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    GithubEventMapping getPayloadMapping(EventType eventType) throws UnrecognizedRequestException;

    String getTeamsDestinationUrl(String repositoryName) throws UnrecognizedRequestException;

    String getDiscordDestinationUrl(String repositoryName) throws UnrecognizedRequestException;

    List<String> getAllRepositories();

}
