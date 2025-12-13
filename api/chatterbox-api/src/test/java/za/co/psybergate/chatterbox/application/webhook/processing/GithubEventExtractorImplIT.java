package za.co.psybergate.chatterbox.application.webhook.processing;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;
import za.co.psybergate.chatterbox.domain.utility.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

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
        GithubEventExtractorImpl.class,
        WebhookConfigurationResolverImpl.class,
        ApplicationConfig.class,
        JsonConverterImpl.class,
        WebhookLogger.class,
        MethodValidationPostProcessor.class,
        LocalValidatorFactoryBean.class,
})
public class GithubEventExtractorImplIT {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private JsonConverter jsonConverter;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @DisplayName("Extractor maps to DTO")
    @Test
    public void givenJsonString_WhenExtract_ThenSuccess() {
        JsonNode jsonNode = jsonConverter.getAsJson(getValidJsonString());
        GithubEventDto eventDto = eventExtractor.extract("push", jsonNode);

        assertNotNull(eventDto);
        assertEquals("push", eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", eventDto.url());
        assertEquals("Test message Is here!", eventDto.urlDisplayText());
        assertEquals("https://outlook.office.com/webhook/...", eventDto.teamsDestination());
    }

    @DisplayName("Unknown Event: Exception")
    @Test
    public void givenJsonString_WithUnknownEvent_WhenExtract_ThenException() {
        JsonNode jsonNode = jsonConverter.getAsJson(jsonStringWithUnknownEvent());
        assertThrows(UnrecognizedRequestException.class,
                () -> eventExtractor.extract("unknownEvent", jsonNode));
    }

    @DisplayName("Missing All JSON keys: Exception")
    @Test
    public void givenIncompleteJsonString_WhenExtract_ThenException() {
        JsonNode jsonNode = jsonConverter.getAsJson(jsonStringWithMissingProperties());
        assertThrows(UnrecognizedRequestException.class,
                () -> eventExtractor.extract("push", jsonNode));
    }

    @DisplayName("Missing Most JSON keys: Exception")
    @Test
    public void givenPartialJsonString_WithRepositoryName_WhenExtract_ThenException() {
        JsonNode jsonNode = jsonConverter.getAsJson(jsonStringWithEventTypeAndRepositoryName());
        assertThrows(ConstraintViolationException.class,
                () -> eventExtractor.extract("push", jsonNode));
    }

    @DisplayName("No UrlDisplayText; then eventType")
    @Test
    public void givenJsonString_WithNoUrlDisplayText_WhenExtract_ThenUrlDisplayTextIsEventType() {
        JsonNode jsonNode = jsonConverter.getAsJson(jsonStringWithNoUrlDisplayText());
        GithubEventDto eventDto = eventExtractor.extract("push", jsonNode);

        assertNotNull(eventDto);
        assertEquals("push", eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("http://localhost:abcd", eventDto.url());
        assertEquals("Push Event", eventDto.urlDisplayText());
        assertEquals("https://outlook.office.com/webhook/...", eventDto.teamsDestination());
    }

    @DisplayName("Long UrlDisplayText is Truncated")
    @Test
    public void givenJsonString_WithLongUrlDisplayText_WhenExtract_ThenUrlDisplayTextIsTruncated(){
        JsonNode jsonNode = jsonConverter.getAsJson(jsonStringWithLongUrlDisplayText());
        GithubEventDto eventDto = eventExtractor.extract("push", jsonNode);

        assertNotNull(eventDto);
        assertEquals("push", eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", eventDto.url());
        assertEquals("https://outlook.office.com/webhook/...", eventDto.teamsDestination());

        assertFalse(eventDto.urlDisplayText().contains("\n"));
        assertTrue(eventDto.urlDisplayText().contains("..."));
    }

    private String jsonStringWithLongUrlDisplayText() {
        String pathToFile = "src/test/resources/payload/github-payload-valid-long-url-display-text.json";
        return jsonConverter.readPayload(pathToFile);
    }

    private String jsonStringWithNoUrlDisplayText() {
        String pathToFile = "src/test/resources/payload/github-payload-invalid-no-url-display-text.json";
        return jsonConverter.readPayload(pathToFile);
    }

    private String jsonStringWithEventTypeAndRepositoryName() {
        String pathToFile = "src/test/resources/payload/github-payload-invalid-contains-event-type-and-repository-name.json";
        return jsonConverter.readPayload(pathToFile);
    }

    private String jsonStringWithMissingProperties() {
        String pathToFile = "src/test/resources/payload/github-payload-invalid-missing-properties.json";
        return jsonConverter.readPayload(pathToFile);
    }

    private String jsonStringWithUnknownEvent() {
        String pathToFile = "src/test/resources/payload/github-payload-invalid-unknown-event-type.json";
        return jsonConverter.readPayload(pathToFile);
    }

    private String getValidJsonString() {
        String pathToFile = "src/test/resources/payload/github-payload-valid.json";
        return jsonConverter.readPayload(pathToFile);
    }

}