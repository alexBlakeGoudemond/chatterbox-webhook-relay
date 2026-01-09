package za.co.psybergate.chatterbox.application.webhook.routing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationDiscordProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationTeamsProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties.DestinationMapping;

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
    public ChatterboxSourceGithubPayloadProperties.EventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException {
        return getPayloadMapping(EventType.get(eventType));
    }

    @Override
    public ChatterboxSourceGithubPayloadProperties.EventMapping getPayloadMapping(EventType eventType) throws UnrecognizedRequestException {
        return payloadProperties.getEventMapping(eventType.name());
    }

    @Override
    public String getTeamsDestinationUrl(String repositoryName) throws UnrecognizedRequestException {
        for (DestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            if (destinationMapping.getName().equals(repositoryName)) {
                return destinationTeamsProperties.getUrl(destinationMapping.getTeamsDestinationChannel());
            }
        }
        throw new UnrecognizedRequestException("Unable to find the destination for " + repositoryName);
    }

    @Override
    public String getDiscordDestinationUrl(String repositoryName) throws UnrecognizedRequestException {
        for (DestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            if (destinationMapping.getName().equals(repositoryName)) {
                return destinationDiscordProperties.getUrl(destinationMapping.getDiscordDestinationChannel());
            }
        }
        throw new UnrecognizedRequestException("Unable to find the destination for " + repositoryName);
    }

    @Override
    public List<String> getAllRepositories() {
        List<String> repositories = new ArrayList<>();
        for (DestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            repositories.add(destinationMapping.getName());
        }
        return repositories;
    }

}
