package za.co.psybergate.chatterbox.application.domain.configuration;

import java.util.Map;

/**
 * Can be paired with {@link za.co.psybergate.chatterbox.adapter.out.delivery.model.DeliveryMapping}
 * for the Map keys
 * */
public record DestinationMapping(
        String source,
        Map<String, String> destinationChannels
) {

}