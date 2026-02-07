package za.co.psybergate.chatterbox.adapter.out.delivery.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryChannelType;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryResult;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.port.out.delivery.DestinationSenderPort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompositeEventDeliveryAdapterTest {

    @Mock
    private DestinationSenderPort teamsSender;

    @Mock
    private DestinationSenderPort discordSender;

    @Mock
    private WebhookConfigurationResolverPort configurationResolver;

    @Mock
    private WebhookPolledEventStorePort polledEventStore;

    @Mock
    private WebhookEventStorePort webhookEventStore;

    private CompositeEventDeliveryAdapter adapter;

    private OutboundEvent event;

    @BeforeEach
    void setUp() {
        adapter = new CompositeEventDeliveryAdapter(
                teamsSender,
                discordSender,
                configurationResolver,
                polledEventStore,
                webhookEventStore
        );
        event = new OutboundEvent(
                1L, "source-1", "push", "Push Event", "repo", "actor", "http://url", "Push to repo", "extra", "{}"
        );
    }

    @Test
    @DisplayName("Should deliver to Teams only when only Teams channel is provided")
    void givenTeamsChannelAndURL_WhenDeliver_ThenTeamsReachedOnly() {
        String channel = "teams-channel";
        String url = "http://teams-webhook-url";
        DestinationMapping mapping = new DestinationMapping("source", Map.of(DeliveryChannelType.NOTIFICATION, channel));
        when(configurationResolver.getDestinationUrl(channel, DeliveryChannelType.NOTIFICATION)).thenReturn(url);
        when(teamsSender.deliver(event, url)).thenReturn(DeliveryResult.SUCCESS);
        adapter.deliver(event, mapping);

        verify(teamsSender).deliver(event, url);
        verifyNoInteractions(discordSender);
        verify(webhookEventStore).storeSuccessfulDelivery(event, channel, url);
        verify(polledEventStore).storeSuccessfulDelivery(event, channel, url);
    }

    @Test
    @DisplayName("Should deliver to Discord only when only Discord channel is provided")
    void givenDiscordChannelAndURL_WhenDeliver_ThenDiscordReachedOnly() {
        String channel = "discord-channel";
        String url = "http://discord-webhook-url";
        DestinationMapping mapping = new DestinationMapping("source", Map.of(DeliveryChannelType.CHAT, channel));
        when(configurationResolver.getDestinationUrl(channel, DeliveryChannelType.CHAT)).thenReturn(url);
        when(discordSender.deliver(event, url)).thenReturn(DeliveryResult.SUCCESS);
        adapter.deliver(event, mapping);

        verify(discordSender).deliver(event, url);
        verifyNoInteractions(teamsSender);
        verify(webhookEventStore).storeSuccessfulDelivery(event, channel, url);
        verify(polledEventStore).storeSuccessfulDelivery(event, channel, url);
    }

    @Test
    @DisplayName("Should deliver to both Teams and Discord when both channels are provided")
    void givenTeamsAndDiscordDetails_WhenDeliver_ThenTeamsAndDiscordReached() {
        String teamsChannel = "teams-channel";
        String teamsUrl = "http://teams-webhook-url";
        String discordChannel = "discord-channel";
        String discordUrl = "http://discord-webhook-url";
        DestinationMapping mapping = new DestinationMapping("source", Map.of(
                DeliveryChannelType.NOTIFICATION, teamsChannel,
                DeliveryChannelType.CHAT, discordChannel
        ));
        when(configurationResolver.getDestinationUrl(teamsChannel, DeliveryChannelType.NOTIFICATION)).thenReturn(teamsUrl);
        when(teamsSender.deliver(event, teamsUrl)).thenReturn(DeliveryResult.SUCCESS);
        when(configurationResolver.getDestinationUrl(discordChannel, DeliveryChannelType.CHAT)).thenReturn(discordUrl);
        when(discordSender.deliver(event, discordUrl)).thenReturn(DeliveryResult.SUCCESS);
        adapter.deliver(event, mapping);

        verify(teamsSender).deliver(event, teamsUrl);
        verify(discordSender).deliver(event, discordUrl);
        verify(webhookEventStore).storeSuccessfulDelivery(event, teamsChannel, teamsUrl);
        verify(webhookEventStore).storeSuccessfulDelivery(event, discordChannel, discordUrl);
    }

    @Test
    @DisplayName("Should store unsuccessful delivery when sender fails")
    void WhenTeamsDeliveryFails_ThenEventPersistedAsUnsuccessful() {
        String channel = "teams-channel";
        String url = "http://teams-webhook-url";
        DestinationMapping mapping = new DestinationMapping("source", Map.of(DeliveryChannelType.NOTIFICATION, channel));
        when(configurationResolver.getDestinationUrl(channel, DeliveryChannelType.NOTIFICATION)).thenReturn(url);
        when(teamsSender.deliver(event, url)).thenReturn(DeliveryResult.FAILURE);
        adapter.deliver(event, mapping);

        // Then
        verify(teamsSender).deliver(event, url);
        verify(webhookEventStore).storeUnsuccessfulDelivery(event, channel, url);
        verify(polledEventStore).storeUnsuccessfulDelivery(event, channel, url);
        verify(webhookEventStore, never()).storeSuccessfulDelivery(any(), any(), any());
    }

}
