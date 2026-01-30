package za.co.psybergate.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.delivery.model.DeliveryChannelDetails;
import za.co.psybergate.chatterbox.adapter.out.teams.model.TeamsAcceptedChannel;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.destinations.teams")
public class ChatterboxDestinationTeamsProperties {

    private List<TeamsAcceptedChannel> acceptedChannel;

    public String getUrl(String teamsChannel) {
        for (DeliveryChannelDetails acceptedChannel : acceptedChannel) {
            if (acceptedChannel.getChannelName().equalsIgnoreCase(teamsChannel)) {
                return acceptedChannel.getWebhookUrl();
            }
        }
        throw new UnrecognizedRequestException("Unable to find teamsChannel: '" + teamsChannel + "'");
    }

}
