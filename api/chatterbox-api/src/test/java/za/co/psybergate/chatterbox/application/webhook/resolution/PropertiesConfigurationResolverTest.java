package za.co.psybergate.chatterbox.application.webhook.resolution;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.configuration.EventPayloadMapping;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryChannelType;
import za.co.psybergate.chatterbox.application.domain.exception.DomainException;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        PropertiesConfigurationResolver.class,
        InfrastructurePropertiesConfig.class,
})
@ActiveProfiles({"test", "bad-properties"})
public class PropertiesConfigurationResolverTest {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private WebhookConfigurationResolverPort resolver;

    @DisplayName("Known eventType succeeds")
    @Test
    public void givenRecognizedEventType_WhenGetPayloadMapping_ThenSuccess() {
        EventPayloadMapping payloadMapping = resolver.getPayloadMapping(WebhookEventType.PUSH);
        assertNotNull(payloadMapping);
    }

    @DisplayName("Known eventType String succeeds")
    @Test
    public void givenRecognizedEventTypeString_WhenGetPayloadMapping_ThenSuccess() {
        EventPayloadMapping payloadMapping = resolver.getPayloadMapping("push");
        assertNotNull(payloadMapping);
    }

    @DisplayName("Unknown eventType gives Exception")
    @Test
    public void givenUnrecognizedEventType_WhenGetPayloadMapping_ThenException() {
        Assertions.assertThrows(DomainException.class,
                () -> resolver.getPayloadMapping("unknownEventType"));
    }

    @DisplayName("Known Teams Destination Channel Name succeeds")
    @Test
    public void givenRecognizedTeamsDestinationChannelName_WhenGetPayloadMapping_ThenSuccess() {
        String destinationUrl = resolver.resolveDestinationUrl("Psybergate-Knowledge-Repository/mentoring_software_foundations", DeliveryChannelType.NOTIFICATION);
        assertNotNull(destinationUrl);
    }

    @DisplayName("Unknown Teams Destination Channel Name gives Exception")
    @Test
    public void givenUnrecognizedTeamsDestinationChannelName_WhenGetPayloadMapping_ThenException() {
        Assertions.assertThrows(UnrecognizedRequestException.class,
                () -> resolver.resolveDestinationUrl("psyAlexBlakeGoudemond/chatterbox/undefined", DeliveryChannelType.NOTIFICATION));
    }

}