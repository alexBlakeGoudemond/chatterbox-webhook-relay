package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.destinations.discord")
public class ChatterboxDestinationDiscordProperties {

    private List<AcceptedChannel> acceptedChannel;

    public String getUrl(String discordChannel) {
        for (AcceptedChannel acceptedChannel : acceptedChannel) {
            if (acceptedChannel.getChannelName().equalsIgnoreCase(discordChannel)) {
                return acceptedChannel.getWebhookUrl();
            }
        }
        throw new UnrecognizedRequestException("Unable to find discordChannel: '" + discordChannel + "'");
    }

    @Data
    public static class AcceptedChannel {

        private String channelName;

        private String webhookUrl;

    }

}
