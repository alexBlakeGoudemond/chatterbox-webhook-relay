package za.co.psybergate.chatterbox.adapter.out.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.application.common.logging.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import za.co.psybergate.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.exception.DomainException;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;
import za.co.psybergate.chatterbox.test.helper.TestConfigurationResolver;

import static org.junit.jupiter.api.Assertions.*;

/// As an alternative to just using the FULL Application Context (`@SpringBootTest()`)
/// we can explicitly include [MethodValidationPostProcessor] and [LocalValidatorFactoryBean].
/// This manually replicates the minimal infrastructure Spring Boot normally auto-configures.
///
/// By doing this:
/// - `@Validated` on GithubEventExtractor works
/// - `@Valid` on the return type triggers
/// - `@NotNull` on the GithubEventDto fields is enforced
@SpringBootTest(classes = {
        GithubWebhookEventMapper.class,
        PropertiesConfigurationResolver.class,
        InfrastructurePropertiesConfig.class,
        JsonFileReader.class,
        JacksonJsonConverter.class,
        Slf4jWebhookLogger.class,
        MethodValidationPostProcessor.class,
        LocalValidatorFactoryBean.class,
        PropertiesConfigurationResolver.class,
        TestConfigurationResolver.class,
})
public class GithubWebhookEventMapperIT {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private OutboundEventMapperPort eventExtractor;

    @Autowired
    private TestConfigurationResolver configurationResolver;

    @DisplayName("Extractor maps to DTO")
    @Test
    public void givenJsonString_WhenMap_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));

        assertNotNull(outboundEvent);
        assertEquals(WebhookEventType.PUSH.name(), outboundEvent.type());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", outboundEvent.repository());
        assertEquals("psyAlexBlakeGoudemond", outboundEvent.actor());
        assertEquals("https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", outboundEvent.url());
        assertEquals("Test message Is here!", outboundEvent.displayText());
        assertEquals("https://outlook.office.com/webhook/...", configurationResolver.getTeamsDestinationUrl(outboundEvent));
    }

    @DisplayName("Unknown Event: Exception")
    @Test
    public void givenJsonString_WithUnknownEvent_WhenMap_ThenException() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadUnknownEvent();
        assertThrows(DomainException.class,
                () -> eventExtractor.map("unknownEvent", RawEventPayload.of(jsonNode)));
    }

    @DisplayName("Missing All JSON keys: Exception")
    @Test
    public void givenIncompleteJsonString_WhenMap_ThenException() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadMissingProperties();
        assertThrows(ConstraintViolationException.class,
                () -> eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode)));
    }

    @DisplayName("Missing Most JSON keys: Exception")
    @Test
    public void givenPartialJsonString_WithRepositoryName_WhenMap_ThenException() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadInvalidEventTypeAndRepositoryName();
        assertThrows(ConstraintViolationException.class,
                () -> eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode)));
    }

    @DisplayName("No UrlDisplayText; then eventType")
    @Test
    public void givenJsonString_WithNoUrlDisplayText_WhenMap_ThenUrlDisplayTextIsEventType() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadNoDisplayText();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));

        assertNotNull(outboundEvent);
        assertEquals(WebhookEventType.PUSH.name(), outboundEvent.type());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", outboundEvent.repository());
        assertEquals("psyAlexBlakeGoudemond", outboundEvent.actor());
        assertEquals("http://localhost:abcd", outboundEvent.url());
        assertEquals("Push Event", outboundEvent.displayText());
        assertEquals("https://outlook.office.com/webhook/...", configurationResolver.getTeamsDestinationUrl(outboundEvent));
    }

    @DisplayName("Long UrlDisplayText is Truncated")
    @Test
    public void givenJsonString_WithLongUrlDisplayText_WhenMap_ThenUrlDisplayTextIsTruncated() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadLongDisplayText();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));

        assertNotNull(outboundEvent);
        assertEquals(WebhookEventType.PUSH.name(), outboundEvent.type());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", outboundEvent.repository());
        assertEquals("psyAlexBlakeGoudemond", outboundEvent.actor());
        assertEquals("https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", outboundEvent.url());
        assertEquals("https://outlook.office.com/webhook/...", configurationResolver.getTeamsDestinationUrl(outboundEvent));

        assertFalse(outboundEvent.displayText().contains("\n"));
        assertTrue(outboundEvent.displayText().contains("..."));
    }

}