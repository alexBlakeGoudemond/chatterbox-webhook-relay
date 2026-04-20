package com.webhook.relay.chatterbox.application.domain.configuration;

import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelType;

import java.util.Map;

public record DestinationMapping(
        String source,
        Map<DeliveryChannelType, String> destinationChannels
) {

}