package za.co.psybergate.chatterbox.application.webhook.resolution;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.exception.DomainException;
import za.co.psybergate.chatterbox.domain.github.model.GithubEventMapping;
import za.co.psybergate.chatterbox.infrastructure.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.infrastructure.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.out.webhook.resolution.WebhookConfigurationResolverImpl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        WebhookConfigurationResolverImpl.class,
        InfrastructurePropertiesConfig.class
})
@ActiveProfiles({"test", "bad-properties"})
public class WebhookConfigurationResolverImplTest {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private WebhookConfigurationResolverPort resolver;

    @DisplayName("Known eventType succeeds")
    @Test
    public void givenRecognizedEventType_WhenGetPayloadMapping_ThenSuccess() {
        GithubEventMapping payloadMapping = resolver.getPayloadMapping(EventType.PUSH);
        assertNotNull(payloadMapping);
    }

    @DisplayName("Known eventType String succeeds")
    @Test
    public void givenRecognizedEventTypeString_WhenGetPayloadMapping_ThenSuccess() {
        GithubEventMapping payloadMapping = resolver.getPayloadMapping("push");
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
        String destinationUrl = resolver.getTeamsDestinationUrl("Psybergate-Knowledge-Repository/mentoring_software_foundations");
        assertNotNull(destinationUrl);
    }

    @DisplayName("Unknown Teams Destination Channel Name gives Exception")
    @Test
    public void givenUnrecognizedTeamsDestinationChannelName_WhenGetPayloadMapping_ThenException() {
        Assertions.assertThrows(UnrecognizedRequestException.class,
                () -> resolver.getTeamsDestinationUrl("psyAlexBlakeGoudemond/chatterbox/undefined"));
    }

}