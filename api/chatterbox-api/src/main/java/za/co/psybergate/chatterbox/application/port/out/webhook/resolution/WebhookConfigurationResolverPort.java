package za.co.psybergate.chatterbox.application.port.out.webhook.resolution;

import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.api.EventType;
import za.co.psybergate.chatterbox.application.domain.github.model.GithubDestinationMapping;
import za.co.psybergate.chatterbox.application.domain.github.model.GithubEventMapping;

import java.util.List;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolverPort {

    GithubEventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    GithubEventMapping getPayloadMapping(EventType eventType) throws UnrecognizedRequestException;

    String getTeamsDestinationUrl(String repositoryName) throws UnrecognizedRequestException;

    String getDiscordDestinationUrl(String repositoryName) throws UnrecognizedRequestException;

    List<String> getAllRepositories();

    List<GithubDestinationMapping> getDestinationMapping();

    String getTeamsUrl(String teamsDestinationChannel);

    String getDiscordUrl(String discordDestinationChannel);

}
