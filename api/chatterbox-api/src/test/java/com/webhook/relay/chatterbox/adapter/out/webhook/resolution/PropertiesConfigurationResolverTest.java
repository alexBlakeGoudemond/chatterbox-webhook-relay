package com.webhook.relay.chatterbox.adapter.out.webhook.resolution;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.webhook.relay.chatterbox.application.common.exception.UnrecognizedRequestException;
import com.webhook.relay.chatterbox.application.domain.configuration.DestinationMapping;
import com.webhook.relay.chatterbox.application.domain.configuration.EventPayloadMapping;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelType;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.domain.exception.DomainException;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxDestinationDiscordProperties;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxDestinationTeamsProperties;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxSourceGithubPayloadProperties;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxSourceGithubRepositoryProperties;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertiesConfigurationResolverTest {

    @Mock
    private ChatterboxSourceGithubPayloadProperties payloadProperties;

    @Mock
    private ChatterboxSourceGithubRepositoryProperties repositoryProperties;

    @Mock
    private ChatterboxDestinationTeamsProperties destinationTeamsProperties;

    @Mock
    private ChatterboxDestinationDiscordProperties destinationDiscordProperties;

    private PropertiesConfigurationResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PropertiesConfigurationResolver(
                payloadProperties,
                repositoryProperties,
                destinationTeamsProperties,
                destinationDiscordProperties
        );
    }

    @DisplayName("Known eventType succeeds")
    @Test
    void givenRecognizedEventType_WhenGetPayloadMapping_ThenSuccess() {
        when(payloadProperties.getEventPayloadMapping("PUSH")).thenReturn(EventPayloadMapping.builder().build());
        EventPayloadMapping payloadMapping = resolver.getPayloadMapping(WebhookEventType.PUSH);
        assertNotNull(payloadMapping);
    }

    @DisplayName("Known eventType String succeeds")
    @Test
    void givenRecognizedEventTypeString_WhenGetPayloadMapping_ThenSuccess() {
        when(payloadProperties.getEventPayloadMapping("PUSH")).thenReturn(EventPayloadMapping.builder().build());
        EventPayloadMapping payloadMapping = resolver.getPayloadMapping("push");
        assertNotNull(payloadMapping);
    }

    @DisplayName("Unknown eventType gives Exception")
    @Test
    void givenUnrecognizedEventType_WhenGetPayloadMapping_ThenException() {
        Assertions.assertThrows(DomainException.class,
                () -> resolver.getPayloadMapping("unknownEventType"));
    }

    @DisplayName("Known Teams Destination Channel Name succeeds")
    @Test
    void givenRecognizedTeamsDestinationChannelName_WhenGetPayloadMapping_ThenSuccess() {
        String repo = "org/repo";
        String channel = "dev-channel";
        String url = "http://teams.webhook";
        DestinationMapping mapping = new DestinationMapping(repo, Map.of(DeliveryChannelType.NOTIFICATION, channel));
        when(repositoryProperties.getDestinationMapping()).thenReturn(List.of(mapping));
        when(destinationTeamsProperties.getUrl(channel)).thenReturn(url);

        String destinationUrl = resolver.resolveDestinationUrl(repo, DeliveryChannelType.NOTIFICATION);
        assertNotNull(destinationUrl);
        Assertions.assertEquals(url, destinationUrl);
    }

    @DisplayName("Unknown Teams Destination Channel Name gives Exception")
    @Test
    void givenUnrecognizedTeamsDestinationChannelName_WhenGetPayloadMapping_ThenException() {
        when(repositoryProperties.getDestinationMapping()).thenReturn(List.of());
        Assertions.assertThrows(UnrecognizedRequestException.class,
                () -> resolver.resolveDestinationUrl("unknown/repo", DeliveryChannelType.NOTIFICATION));
    }

}