package za.co.psybergate.chatterbox.adapter.out.webhook.resolution;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.adapter.out.delivery.model.DeliveryMapping;
import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventMapping;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxDestinationDiscordProperties;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxDestinationTeamsProperties;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxSourceGithubRepositoryProperties;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PropertiesConfigurationResolver implements WebhookConfigurationResolverPort {

    private final ChatterboxSourceGithubPayloadProperties payloadProperties;

    private final ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    private final ChatterboxDestinationTeamsProperties destinationTeamsProperties;

    private final ChatterboxDestinationDiscordProperties destinationDiscordProperties;

    @Override
    public GithubEventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException {
        return getPayloadMapping(WebhookEventType.get(eventType));
    }

    @Override
    public GithubEventMapping getPayloadMapping(WebhookEventType webhookEventType) throws UnrecognizedRequestException {
        return payloadProperties.getEventMapping(webhookEventType.name());
    }

    @Override
    public String resolveTeamsUrl(String repositoryName) throws UnrecognizedRequestException {
        for (DestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            if (!destinationMapping.source().equals(repositoryName)) {
                continue;
            }
            return destinationTeamsProperties.getUrl(destinationMapping.destinationChannels().get(DeliveryMapping.MS_TEAMS.name()));
        }
        throw new UnrecognizedRequestException("Unable to find the destination for " + repositoryName);
    }

    @Override
    public String resolveDiscordUrl(String repositoryName) throws UnrecognizedRequestException {
        for (DestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            if (!destinationMapping.source().equalsIgnoreCase(repositoryName)) {
                continue;
            }
            return destinationDiscordProperties.getUrl(destinationMapping.destinationChannels().get(DeliveryMapping.DISCORD.name()));
        }
        throw new UnrecognizedRequestException("Unable to find the destination for " + repositoryName);
    }

    @Override
    public List<String> getAllRepositories() {
        List<String> repositories = new ArrayList<>();
        for (DestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            repositories.add(destinationMapping.source());
        }
        return repositories;
    }

    @Override
    public List<DestinationMapping> getDestinationMapping() {
        return repositoryProperties.getDestinationMapping();
    }

    @Override
    public String getTeamsUrl(String teamsDestinationChannel) {
        return destinationTeamsProperties.getUrl(teamsDestinationChannel);
    }

    @Override
    public String getDiscordUrl(String discordDestinationChannel) {
        return destinationDiscordProperties.getUrl(discordDestinationChannel);
    }

}
