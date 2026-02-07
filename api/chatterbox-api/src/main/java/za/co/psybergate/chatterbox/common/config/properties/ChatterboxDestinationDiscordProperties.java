package za.co.psybergate.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.adapter.out.discord.model.DiscordAcceptedChannel;
import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryChannelDetails;

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
