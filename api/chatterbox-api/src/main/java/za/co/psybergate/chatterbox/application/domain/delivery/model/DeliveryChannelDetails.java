package za.co.psybergate.chatterbox.application.domain.delivery.model;

import lombok.Data;

/**
 * Common details for delivery channels.
 */
@Data
public class DeliveryChannelDetails {

    private String channelName;

    private String webhookUrl;

}
