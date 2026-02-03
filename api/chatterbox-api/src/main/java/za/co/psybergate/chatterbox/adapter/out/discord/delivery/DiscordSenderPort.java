package za.co.psybergate.chatterbox.adapter.out.discord.delivery;

import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryResult;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;

public interface DiscordSenderPort {

    DeliveryResult deliver(OutboundEvent dto, String discordDestination);

}
