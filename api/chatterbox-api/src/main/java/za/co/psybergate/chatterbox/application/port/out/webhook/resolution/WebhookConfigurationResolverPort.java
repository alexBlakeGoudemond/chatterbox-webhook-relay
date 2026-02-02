package za.co.psybergate.chatterbox.application.port.out.webhook.resolution;

import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventMapping;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;

import java.util.List;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolverPort {

    GithubEventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    GithubEventMapping getPayloadMapping(WebhookEventType webhookEventType) throws UnrecognizedRequestException;

    String resolveTeamsUrl(String repositoryName) throws UnrecognizedRequestException;

    String resolveDiscordUrl(String repositoryName) throws UnrecognizedRequestException;

    List<String> getAllRepositories();

    List<DestinationMapping> getDestinationMapping();

    String getTeamsUrl(String teamsDestinationChannel);

    String getDiscordUrl(String discordDestinationChannel);

}
