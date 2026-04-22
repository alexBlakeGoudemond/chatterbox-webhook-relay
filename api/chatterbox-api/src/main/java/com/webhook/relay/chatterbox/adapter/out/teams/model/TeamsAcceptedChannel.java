package com.webhook.relay.chatterbox.adapter.out.teams.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelDetails;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamsAcceptedChannel extends DeliveryChannelDetails {

}
