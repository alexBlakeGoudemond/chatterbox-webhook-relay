package za.co.psybergate.chatterbox.application.domain.configuration;

import za.co.psybergate.chatterbox.adapter.out.delivery.model.DeliveryMapping;

import java.util.Map;

public record DestinationMapping(
        String source,
        Map<DeliveryMapping, String> destinationChannels
) {

}