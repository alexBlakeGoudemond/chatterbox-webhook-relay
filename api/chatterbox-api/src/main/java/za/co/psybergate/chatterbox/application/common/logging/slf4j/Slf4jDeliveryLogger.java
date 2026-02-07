package za.co.psybergate.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.detail.DeliveryLogger;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

@Slf4j
@Component
public class Slf4jDeliveryLogger implements DeliveryLogger {

    @Override
    public void logSendingDtoToDestination(OutboundEvent outboundEvent, String destination) {
        log.info("[Delivery] Sending '{}' to destination '{}'", outboundEvent.displayText(), destination);
    }
}
