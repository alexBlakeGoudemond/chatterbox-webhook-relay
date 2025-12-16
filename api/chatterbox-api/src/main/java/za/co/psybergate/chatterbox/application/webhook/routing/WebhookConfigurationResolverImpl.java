package za.co.psybergate.chatterbox.application.webhook.routing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDestinationTeamsProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubRepositoryProperties;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;

@Component
@RequiredArgsConstructor
public class WebhookConfigurationResolverImpl implements WebhookConfigurationResolver {

    private final ChatterboxSourceGithubPayloadProperties payloadProperties;

    private final ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    private final ChatterboxDestinationTeamsProperties destinationTeamsProperties;

    @Override
    public ChatterboxSourceGithubPayloadProperties.EventMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException {
        return payloadProperties.getEventMapping(eventType);
    }

    @Override
    public String getDestinationUrl(String repositoryName) throws UnrecognizedRequestException {
        for (ChatterboxSourceGithubRepositoryProperties.DestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            if (destinationMapping.getName().equals(repositoryName)) {
                return destinationTeamsProperties.getUrl(destinationMapping.getDestinationChannel());
            }
        }
        throw new UnrecognizedRequestException("Unable to find the destination for " + repositoryName);
    }

}
