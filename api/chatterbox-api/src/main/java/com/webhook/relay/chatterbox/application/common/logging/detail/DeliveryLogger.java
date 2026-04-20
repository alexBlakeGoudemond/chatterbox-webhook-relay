package com.webhook.relay.chatterbox.application.common.logging.detail;

import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;

public interface DeliveryLogger {

    void logSendingDtoToDestination(OutboundEvent outboundEvent, String destination);

}
