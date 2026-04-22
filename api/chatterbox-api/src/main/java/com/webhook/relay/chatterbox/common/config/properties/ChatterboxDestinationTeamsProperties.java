package com.webhook.relay.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.webhook.relay.chatterbox.adapter.out.teams.model.TeamsAcceptedChannel;
import com.webhook.relay.chatterbox.application.common.exception.UnrecognizedRequestException;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelDetails;

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
