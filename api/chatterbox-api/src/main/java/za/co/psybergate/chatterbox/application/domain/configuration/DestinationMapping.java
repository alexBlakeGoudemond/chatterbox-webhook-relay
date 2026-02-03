package za.co.psybergate.chatterbox.application.domain.configuration;

import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryChannelType;

import java.util.Map;

public record DestinationMapping(
        String source,
        Map<DeliveryChannelType, String> destinationChannels
) {

}