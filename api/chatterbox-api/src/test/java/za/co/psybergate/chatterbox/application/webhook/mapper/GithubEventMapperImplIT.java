package za.co.psybergate.chatterbox.application.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.exception.DomainException;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.infrastructure.in.web.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.in.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapperImpl;
import za.co.psybergate.chatterbox.infrastructure.adapter.webhook.resolution.WebhookConfigurationResolverImpl;
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
        GithubEventMapperImpl.class,
        WebhookConfigurationResolverImpl.class,
        ApplicationConfig.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        WebhookLoggerImpl.class,
        MethodValidationPostProcessor.class,
        LocalValidatorFactoryBean.class,
        WebhookConfigurationResolverImpl.class,
        TestConfigurationResolver.class,
})
public class GithubEventMapperImplIT {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventMapper eventExtractor;

    @Autowired
    private TestConfigurationResolver configurationResolver;

    @DisplayName("Extractor maps to DTO")
    @Test
    public void givenJsonString_WhenMap_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.map(EventType.PUSH, jsonNode);

        assertNotNull(eventDto);
        assertEquals(EventType.PUSH, eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", eventDto.url());
        assertEquals("Test message Is here!", eventDto.urlDisplayText());
        assertEquals("https://outlook.office.com/webhook/...", configurationResolver.getTeamsDestinationUrl(eventDto));
    }

    @DisplayName("Unknown Event: Exception")
    @Test
    public void givenJsonString_WithUnknownEvent_WhenMap_ThenException() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadUnknownEvent();
        assertThrows(DomainException.class,
                () -> eventExtractor.map("unknownEvent", jsonNode));
    }

    @DisplayName("Missing All JSON keys: Exception")
    @Test
    public void givenIncompleteJsonString_WhenMap_ThenException() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadMissingProperties();
        assertThrows(ConstraintViolationException.class,
                () -> eventExtractor.map(EventType.PUSH, jsonNode));
    }

    @DisplayName("Missing Most JSON keys: Exception")
    @Test
    public void givenPartialJsonString_WithRepositoryName_WhenMap_ThenException() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadInvalidEventTypeAndRepositoryName();
        assertThrows(ConstraintViolationException.class,
                () -> eventExtractor.map(EventType.PUSH, jsonNode));
    }

    @DisplayName("No UrlDisplayText; then eventType")
    @Test
    public void givenJsonString_WithNoUrlDisplayText_WhenMap_ThenUrlDisplayTextIsEventType() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadNoDisplayText();
        GithubEventDto eventDto = eventExtractor.map(EventType.PUSH, jsonNode);

        assertNotNull(eventDto);
        assertEquals(EventType.PUSH, eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("http://localhost:abcd", eventDto.url());
        assertEquals("Push Event", eventDto.urlDisplayText());
        assertEquals("https://outlook.office.com/webhook/...", configurationResolver.getTeamsDestinationUrl(eventDto));
    }

    @DisplayName("Long UrlDisplayText is Truncated")
    @Test
    public void givenJsonString_WithLongUrlDisplayText_WhenMap_ThenUrlDisplayTextIsTruncated() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadLongDisplayText();
        GithubEventDto eventDto = eventExtractor.map(EventType.PUSH, jsonNode);

        assertNotNull(eventDto);
        assertEquals(EventType.PUSH, eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", eventDto.url());
        assertEquals("https://outlook.office.com/webhook/...", configurationResolver.getTeamsDestinationUrl(eventDto));

        assertFalse(eventDto.urlDisplayText().contains("\n"));
        assertTrue(eventDto.urlDisplayText().contains("..."));
    }

}