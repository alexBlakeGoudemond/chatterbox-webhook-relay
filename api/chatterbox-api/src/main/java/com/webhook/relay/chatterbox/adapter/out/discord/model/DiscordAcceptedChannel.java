package com.webhook.relay.chatterbox.adapter.out.discord.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelDetails;

@EqualsAndHashCode(callSuper = true)
@Data
public class DiscordAcceptedChannel extends DeliveryChannelDetails {

}
