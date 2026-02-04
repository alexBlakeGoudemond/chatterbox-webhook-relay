package za.co.psybergate.chatterbox.adapter.out.delivery.model;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryChannelType;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryResult;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.port.out.delivery.DestinationSenderPort;
import za.co.psybergate.chatterbox.application.port.out.delivery.EventDeliveryPort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

@Component
public class CompositeEventDeliveryAdapter implements EventDeliveryPort {

    private final DestinationSenderPort teamsSender;

    private final DestinationSenderPort discordSender;

    private final WebhookConfigurationResolverPort configurationResolver;

    private final WebhookPolledEventStorePort polledEventStore;

    private final WebhookEventStorePort webhookEventStore;

    public CompositeEventDeliveryAdapter(@Qualifier("teamsWebhookSender") DestinationSenderPort teamsSender,
                                         @Qualifier("discordWebhookSender") DestinationSenderPort discordSender,
                                         WebhookConfigurationResolverPort configurationResolver,
                                         WebhookPolledEventStorePort polledEventStore,
                                         WebhookEventStorePort webhookEventStore) {
        this.teamsSender = teamsSender;
        this.discordSender = discordSender;
        this.configurationResolver = configurationResolver;
        this.polledEventStore = polledEventStore;
        this.webhookEventStore = webhookEventStore;
    }

    /// The Adapter knows that for this business use-case:
    /// NOTIFICATION -> MS_TEAMS
    /// CHAT -> DISCORD
    @Override
    public void deliver(OutboundEvent event, DestinationMapping mapping) {
        String teamsChannel = mapping.destinationChannels().get(DeliveryChannelType.NOTIFICATION);
        if (teamsChannel != null) {
            deliverToTeams(event, teamsChannel);
        }
        String discordChannel = mapping.destinationChannels().get(DeliveryChannelType.CHAT);
        if (discordChannel != null) {
            deliverToDiscord(event, discordChannel);
        }
    }

    private void deliverToTeams(OutboundEvent event, String channel) {
        String url = configurationResolver.getDestinationUrl(channel, DeliveryChannelType.NOTIFICATION);
        DeliveryResult result = teamsSender.deliver(event, url);

        storeResult(event, channel, url, result);
    }

    private void deliverToDiscord(OutboundEvent event, String channel) {
        String url = configurationResolver.getDestinationUrl(channel, DeliveryChannelType.CHAT);
        DeliveryResult result = discordSender.deliver(event, url);

        storeResult(event, channel, url, result);
    }

    private void storeResult(
            OutboundEvent event,
            String channel,
            String url,
            DeliveryResult result
    ) {
        if (result == DeliveryResult.SUCCESS) {
            webhookEventStore.storeSuccessfulDelivery(event, channel, url);
            polledEventStore.storeSuccessfulDelivery(event, channel, url);
        } else {
            webhookEventStore.storeUnsuccessfulDelivery(event, channel, url);
            polledEventStore.storeUnsuccessfulDelivery(event, channel, url);
        }
    }

}
