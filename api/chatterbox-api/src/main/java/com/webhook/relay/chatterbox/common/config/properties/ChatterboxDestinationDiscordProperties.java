package com.webhook.relay.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.webhook.relay.chatterbox.adapter.out.discord.model.DiscordAcceptedChannel;
import com.webhook.relay.chatterbox.application.common.exception.UnrecognizedRequestException;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelDetails;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.destinations.discord")
public class ChatterboxDestinationDiscordProperties {

    private List<DiscordAcceptedChannel> acceptedChannel;

    public String getUrl(String discordChannel) {
        for (DeliveryChannelDetails acceptedChannel : acceptedChannel) {
            if (acceptedChannel.getChannelName().equalsIgnoreCase(discordChannel)) {
                return acceptedChannel.getWebhookUrl();
            }
        }
        throw new UnrecognizedRequestException("Unable to find discordChannel: '" + discordChannel + "'");
    }

}
