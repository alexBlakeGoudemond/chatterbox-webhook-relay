package com.webhook.relay.chatterbox.application.port.out.delivery;

import com.webhook.relay.chatterbox.application.domain.configuration.DestinationMapping;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;

public interface EventDeliveryPort {

    void deliver(OutboundEvent event, DestinationMapping mapping);

}
