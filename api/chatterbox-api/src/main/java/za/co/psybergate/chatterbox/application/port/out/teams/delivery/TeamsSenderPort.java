package za.co.psybergate.chatterbox.application.port.out.teams.delivery;

import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryResult;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

public interface TeamsSenderPort {

    DeliveryResult deliver(OutboundEvent dto, String teamsDestination);


}
