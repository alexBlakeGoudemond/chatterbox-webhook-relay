package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.destination.teams")
public class ChatterboxDestinationTeamsProperties {

    private List<AcceptedChannel> acceptedChannel;

    public String getUrl(String teamsChannel) {
        for (AcceptedChannel acceptedChannel : acceptedChannel) {
            if (acceptedChannel.getChannelName().equalsIgnoreCase(teamsChannel)) {
                return acceptedChannel.getWebhookUrl();
            }
        }
        throw new UnrecognizedRequestException("Unable to find teansChannel: '" + teamsChannel + "'");
    }

    @Data
    public static class AcceptedChannel {

        private String channelName;

        private String webhookUrl;

    }

}
