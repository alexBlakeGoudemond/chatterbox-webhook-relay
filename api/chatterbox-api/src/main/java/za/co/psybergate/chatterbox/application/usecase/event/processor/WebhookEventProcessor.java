package za.co.psybergate.chatterbox.application.usecase.event.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.adapter.out.delivery.model.DeliveryMapping;
import za.co.psybergate.chatterbox.application.common.event.processor.EventProcessor;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryResult;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;
import za.co.psybergate.chatterbox.application.port.out.discord.delivery.DiscordSenderPort;
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.teams.delivery.TeamsSenderPort;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

@Service
@RequiredArgsConstructor
@Transactional
public class WebhookEventProcessor implements EventProcessor {

    private final WebhookLogger webhookLogger;

    private final TeamsSenderPort teamsSender;

    private final DiscordSenderPort discordSender;

    private final WebhookConfigurationResolverPort configurationResolver;

    private final GithubPolledEventStorePort polledEventStore;

    private final WebhookEventStorePort webhookEventStore;

    @Override
    public void processWebhookEvents() {
        for (DestinationMapping mapping : configurationResolver.getDestinationMapping()) {
            webhookLogger.logProcessingEvents(mapping);
            processWebhookEvents(mapping);
        }
    }

    @Override
    public void processPolledEvents() {
        for (DestinationMapping mapping : configurationResolver.getDestinationMapping()) {
            webhookLogger.logProcessingEvents(mapping);
            processPolledEvents(mapping);
        }
    }

    private void processWebhookEvents(DestinationMapping mapping) {
        for (WebhookEventReceivedDto event : webhookEventStore.getUnprocessedWebhooks(mapping.source())) {
            OutboundEvent outbound = mapToOutboundEvent(event);
            deliver(outbound, mapping);
            webhookEventStore.markProcessed(outbound, WebhookEventStatus.PROCESSED_SUCCESS);
        }
    }

    private void processPolledEvents(DestinationMapping mapping) {
        for (WebhookPolledEventReceivedDto event : polledEventStore.getUnprocessedEvents(mapping.source())) {
            OutboundEvent outbound = mapToOutboundEvent(event);
            deliver(outbound, mapping);
            polledEventStore.markProcessed(outbound, WebhookEventStatus.PROCESSED_SUCCESS);
        }
    }

    // TODO BlakeGoudemond 2026/02/02 | this shouldnt be here - find another way to achieve this without moving this class
    private void deliver(OutboundEvent event, DestinationMapping mapping) {
        deliverToTeams(event, mapping.destinationChannels().get(DeliveryMapping.MS_TEAMS));
        deliverToDiscord(event, mapping.destinationChannels().get(DeliveryMapping.DISCORD));
    }

    private void deliverToTeams(OutboundEvent event, String channel) {
        String url = configurationResolver.resolveTeamsUrl(channel);
        DeliveryResult result = teamsSender.deliver(event, url);

        storeResult(event, channel, url, result, webhookEventStore, polledEventStore);
    }

    private void deliverToDiscord(OutboundEvent event, String channel) {
        String url = configurationResolver.resolveDiscordUrl(channel);
        DeliveryResult result = discordSender.deliver(event, url);

        storeResult(event, channel, url, result, webhookEventStore, polledEventStore);
    }

    private void storeResult(
            OutboundEvent event,
            String channel,
            String url,
            DeliveryResult result,
            WebhookEventStorePort webhookStore,
            GithubPolledEventStorePort polledStore
    ) {
        if (result == DeliveryResult.SUCCESS) {
            webhookStore.storeSuccessfulDelivery(event, channel, url);
            polledStore.storeSuccessfulDelivery(event, channel, url);
        } else {
            webhookStore.storeUnsuccessfulDelivery(event, channel, url);
            polledStore.storeUnsuccessfulDelivery(event, channel, url);
        }
    }

    private OutboundEvent mapToOutboundEvent(WebhookEventReceivedDto event) {
        return new OutboundEvent(
                event.id(),
                event.webhookId(),
                event.webhookEventType().name(),
                event.displayName(),
                event.repositoryFullName(),
                event.senderName(),
                event.eventUrl(),
                event.eventUrlDisplayText(),
                event.extraDetail(),
                event.payload()
        );
    }

    private OutboundEvent mapToOutboundEvent(WebhookPolledEventReceivedDto event) {
        return new OutboundEvent(
                event.id(),
                event.sourceId(),
                event.webhookEventType().name(),
                event.displayName(),
                event.repositoryFullName(),
                event.senderName(),
                event.eventUrl(),
                event.eventUrlDisplayText(),
                event.extraDetail(),
                event.payload()
        );
    }

}
