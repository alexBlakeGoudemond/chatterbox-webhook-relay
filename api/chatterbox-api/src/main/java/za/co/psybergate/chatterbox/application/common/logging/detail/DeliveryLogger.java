package za.co.psybergate.chatterbox.application.common.logging.detail;

import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

public interface DeliveryLogger {

    void logSendingDtoToDestination(OutboundEvent outboundEvent, String destination);

}
