package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.destination.teams")
public class ChatterboxDestinationTeamsProperties {

    private List<AcceptedChannel> channelsAccepted;

    @Data
    public static class AcceptedChannel {

        private String channelName;

        private String webhookUrl;

    }

}
