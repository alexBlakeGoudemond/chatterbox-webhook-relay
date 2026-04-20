package com.webhook.relay.chatterbox.application.port.out.webhook.resolution;

import com.webhook.relay.chatterbox.application.common.exception.UnrecognizedRequestException;
import com.webhook.relay.chatterbox.application.domain.configuration.DestinationMapping;
import com.webhook.relay.chatterbox.application.domain.configuration.EventPayloadMapping;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelType;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;

import java.util.List;

/// resolves configuration, handles destination and template mapping
public interface WebhookConfigurationResolverPort {

    EventPayloadMapping getPayloadMapping(String eventType) throws UnrecognizedRequestException;

    EventPayloadMapping getPayloadMapping(WebhookEventType webhookEventType) throws UnrecognizedRequestException;

    String resolveDestinationUrl(String repositoryName, DeliveryChannelType channelType) throws UnrecognizedRequestException;

    List<String> getAllRepositories();

    List<DestinationMapping> getDestinationMapping();

    String getDestinationUrl(String destinationChannel, DeliveryChannelType channelType);

}
