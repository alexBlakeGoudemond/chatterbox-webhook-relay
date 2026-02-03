package za.co.psybergate.chatterbox.application.port.out.webhook.resolution;

import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.configuration.EventPayloadMapping;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryChannelType;

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
