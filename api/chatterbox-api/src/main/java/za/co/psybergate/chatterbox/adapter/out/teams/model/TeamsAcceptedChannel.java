package za.co.psybergate.chatterbox.adapter.out.teams.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import za.co.psybergate.chatterbox.application.domain.delivery.model.DeliveryChannelDetails;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamsAcceptedChannel extends DeliveryChannelDetails {
}
