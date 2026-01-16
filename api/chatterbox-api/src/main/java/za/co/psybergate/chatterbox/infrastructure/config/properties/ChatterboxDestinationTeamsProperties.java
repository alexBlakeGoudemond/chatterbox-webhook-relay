package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.teams.model.TeamsAcceptedChannel;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.destinations.teams")
public class ChatterboxDestinationTeamsProperties {

    private List<TeamsAcceptedChannel> acceptedChannel;

    public String getUrl(String teamsChannel) {
        for (TeamsAcceptedChannel acceptedChannel : acceptedChannel) {
            if (acceptedChannel.getChannelName().equalsIgnoreCase(teamsChannel)) {
                return acceptedChannel.getWebhookUrl();
            }
        }
        throw new UnrecognizedRequestException("Unable to find teamsChannel: '" + teamsChannel + "'");
    }

}
