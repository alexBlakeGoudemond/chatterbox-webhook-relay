package za.co.psybergate.chatterbox.application.port.out.delivery;

import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryResult;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

public interface DestinationSenderPort {

    DeliveryResult deliver(OutboundEvent dto, String destinationUrl);

}
