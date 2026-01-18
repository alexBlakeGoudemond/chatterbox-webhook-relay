package za.co.psybergate.chatterbox.infrastructure.out.webhook.resolution;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolver;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.github.model.GithubDestinationMapping;
import za.co.psybergate.chatterbox.domain.github.model.GithubEventMapping;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationDiscordProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationTeamsProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebhookConfigurationResolverImpl implements WebhookConfigurationResolver {

    private final ChatterboxSourceGithubPayloadProperties payloadProperties;

    private final ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    private final ChatterboxDestinationTeamsProperties destinationTeamsProperties;

    private final ChatterboxDestinationDiscordProperties destinationDiscordProperties;

    @Override
    public GithubEventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException {
        return getPayloadMapping(EventType.get(eventType));
    }

    @Override
    public GithubEventMapping getPayloadMapping(EventType eventType) throws UnrecognizedRequestException {
        return payloadProperties.getEventMapping(eventType.name());
    }

    @Override
    public String getTeamsDestinationUrl(String repositoryName) throws UnrecognizedRequestException {
        for (GithubDestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            if (destinationMapping.getName().equals(repositoryName)) {
                return destinationTeamsProperties.getUrl(destinationMapping.getTeamsDestinationChannel());
            }
        }
        throw new UnrecognizedRequestException("Unable to find the destination for " + repositoryName);
    }

    @Override
    public String getDiscordDestinationUrl(String repositoryName) throws UnrecognizedRequestException {
        for (GithubDestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            if (destinationMapping.getName().equals(repositoryName)) {
                return destinationDiscordProperties.getUrl(destinationMapping.getDiscordDestinationChannel());
            }
        }
        throw new UnrecognizedRequestException("Unable to find the destination for " + repositoryName);
    }

    @Override
    public List<String> getAllRepositories() {
        List<String> repositories = new ArrayList<>();
        for (GithubDestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            repositories.add(destinationMapping.getName());
        }
        return repositories;
    }

    @Override
    public List<GithubDestinationMapping> getDestinationMapping() {
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
