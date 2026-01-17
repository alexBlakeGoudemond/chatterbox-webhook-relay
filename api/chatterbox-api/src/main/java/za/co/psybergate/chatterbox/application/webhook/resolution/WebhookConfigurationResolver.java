package za.co.psybergate.chatterbox.application.webhook.resolution;

import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.github.model.GithubEventMapping;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.util.List;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolver {

    // TODO BlakeGoudemond 2026/01/17 | add test case for this
    GithubEventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    GithubEventMapping getPayloadMapping(EventType eventType) throws UnrecognizedRequestException;

    String getTeamsDestinationUrl(String repositoryName) throws UnrecognizedRequestException;

    String getDiscordDestinationUrl(String repositoryName) throws UnrecognizedRequestException;

    List<String> getAllRepositories();

}
