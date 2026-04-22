package com.webhook.relay.chatterbox.adapter.out.webhook.resolution;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.common.exception.UnrecognizedRequestException;
import com.webhook.relay.chatterbox.application.domain.configuration.DestinationMapping;
import com.webhook.relay.chatterbox.application.domain.configuration.EventPayloadMapping;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelType;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxDestinationDiscordProperties;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxDestinationTeamsProperties;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxSourceGithubPayloadProperties;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxSourceGithubRepositoryProperties;

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
