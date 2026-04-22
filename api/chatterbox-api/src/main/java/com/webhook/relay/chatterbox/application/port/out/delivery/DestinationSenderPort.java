package com.webhook.relay.chatterbox.application.port.out.delivery;

import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryResult;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;

public interface DestinationSenderPort {

    DeliveryResult deliver(OutboundEvent dto, String destinationUrl);

}
