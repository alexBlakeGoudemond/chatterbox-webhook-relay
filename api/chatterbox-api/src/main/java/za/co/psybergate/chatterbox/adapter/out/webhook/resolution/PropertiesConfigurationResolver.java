package za.co.psybergate.chatterbox.adapter.out.webhook.resolution;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.configuration.EventPayloadMapping;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryChannelType;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
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
    public EventPayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException {
        return getPayloadMapping(WebhookEventType.get(eventType));
    }

    @Override
    public EventPayloadMapping getPayloadMapping(WebhookEventType webhookEventType) throws UnrecognizedRequestException {
        return payloadProperties.getEventPayloadMapping(webhookEventType.name());
    }

    @Override
    public String resolveDestinationUrl(String repositoryName, DeliveryChannelType channelType) throws UnrecognizedRequestException {
        for (DestinationMapping destinationMapping : repositoryProperties.getDestinationMapping()) {
            if (!destinationMapping.source().equalsIgnoreCase(repositoryName)) {
                continue;
            }
            String channel = destinationMapping.destinationChannels().get(channelType);
            return getDestinationUrl(channel, channelType);
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
    public String getDestinationUrl(String destinationChannel, DeliveryChannelType channelType) {
        return switch (channelType) {
            case NOTIFICATION -> destinationTeamsProperties.getUrl(destinationChannel);
            case CHAT -> destinationDiscordProperties.getUrl(destinationChannel);
            default -> throw new UnrecognizedRequestException("Unsupported channel type " + channelType);
        };
    }

}
