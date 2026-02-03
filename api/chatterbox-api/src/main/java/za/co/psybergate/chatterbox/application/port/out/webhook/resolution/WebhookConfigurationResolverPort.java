package za.co.psybergate.chatterbox.application.port.out.webhook.resolution;

import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.configuration.EventPayloadMapping;

import java.util.List;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolverPort {

    EventPayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    EventPayloadMapping getPayloadMapping(WebhookEventType webhookEventType) throws UnrecognizedRequestException;

    String resolveTeamsUrl(String repositoryName) throws UnrecognizedRequestException;

    String resolveDiscordUrl(String repositoryName) throws UnrecognizedRequestException;

    List<String> getAllRepositories();

    List<DestinationMapping> getDestinationMapping();

    String getTeamsUrl(String teamsDestinationChannel);

    String getDiscordUrl(String discordDestinationChannel);

}
