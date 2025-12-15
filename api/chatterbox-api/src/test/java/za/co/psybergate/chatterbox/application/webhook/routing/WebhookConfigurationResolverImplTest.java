package za.co.psybergate.chatterbox.application.webhook.routing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties.EventMapping;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        WebhookConfigurationResolverImpl.class,
        ApplicationConfig.class
})
@ActiveProfiles({"test", "bad-properties"})
public class WebhookConfigurationResolverImplTest {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private WebhookConfigurationResolver resolver;

    @DisplayName("Known eventType succeeds")
    @Test
    public void givenRecognizedEventType_WhenGetPayloadMapping_ThenSuccess() {
        EventMapping payloadMapping = resolver.getPayloadMapping("push");
        assertNotNull(payloadMapping);
    }

    @DisplayName("Unknown eventType gives Exception")
    @Test
    public void givenUnrecognizedEventType_WhenGetPayloadMapping_ThenException() {
        Assertions.assertThrows(UnrecognizedRequestException.class,
                () -> resolver.getPayloadMapping("unknownEventType"));
    }

    @DisplayName("Known Teams Destination Channel Name succeeds")
    @Test
    public void givenRecognizedTeamsDestinationChannelName_WhenGetPayloadMapping_ThenSuccess() {
        String destinationUrl = resolver.getDestinationUrl("Psybergate-Knowledge-Repository/mentoring_software_foundations");
        assertNotNull(destinationUrl);
    }

    @DisplayName("Unknown Teams Destination Channel Name gives Exception")
    @Test
    public void givenUnrecognizedTeamsDestinationChannelName_WhenGetPayloadMapping_ThenException() {
        Assertions.assertThrows(UnrecognizedRequestException.class,
                () -> resolver.getDestinationUrl("psyAlexBlakeGoudemond/chatterbox/undefined"));
    }

}