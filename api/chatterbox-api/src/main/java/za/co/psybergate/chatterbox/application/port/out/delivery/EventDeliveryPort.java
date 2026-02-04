package za.co.psybergate.chatterbox.application.port.out.delivery;

import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

public interface EventDeliveryPort {

    void deliver(OutboundEvent event, DestinationMapping mapping);

}
