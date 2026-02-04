package za.co.psybergate.chatterbox.adapter.out.discord.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryChannelDetails;

@EqualsAndHashCode(callSuper = true)
@Data
public class DiscordAcceptedChannel extends DeliveryChannelDetails {
}
