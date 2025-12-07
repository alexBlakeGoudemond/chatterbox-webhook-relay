package za.co.psybergate.chatterbox.application.webhook.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import za.co.psybergate.chatterbox.application.webhook.validator.WebhookValidatorImpl;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.utility.ConversionUtilities;
import za.co.psybergate.chatterbox.domain.utility.ConversionUtilitiesImpl;
import za.co.psybergate.chatterbox.domain.utility.EncryptionUtilitiesImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
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
/// - this test throws [ConstraintViolationException] for nulls
@SpringBootTest(classes = {
        GithubEventExtractor.class,
        WebhookValidatorImpl.class,
        ApplicationConfig.class,
        ConversionUtilitiesImpl.class,
        WebhookFilter.class,
        WebhookLogger.class,
        EncryptionUtilitiesImpl.class,
        MethodValidationPostProcessor.class,
        LocalValidatorFactoryBean.class,
})
public class GithubEventExtractorTest {

    @Autowired
    private ConversionUtilities conversionUtilities;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;  // Mocked so Spring can inject it

    @DisplayName("Extractor maps to DTO")
    @Test
    public void givenJsonString_WhenExtract_ThenSuccess() {
        JsonNode jsonNode = conversionUtilities.getAsJson(getValidJsonString());
        GithubEventDto eventDto = eventExtractor.extract("push", jsonNode);

        assertNotNull(eventDto);
        assertEquals("push", eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("http://localhost:abcd", eventDto.url());
        assertEquals("Test message Is here!", eventDto.urlDisplayText());
    }

    @DisplayName("Unknown Event: FORBIDDEN")
    @Test
    public void givenJsonString_WithUnknownEvent_WhenExtract_ThenForbidden() {
        JsonNode jsonNode = conversionUtilities.getAsJson(jsonStringWithUnknownEvent());
        assertThrows(UnrecognizedRequestException.class,
                () -> eventExtractor.extract("unknownEvent", jsonNode));
    }

    @DisplayName("Missing JSON keys: Exception")
    @Test
    public void givenIncompleteJsonString_WhenExtract_ThenFailure() {
        JsonNode jsonNode = conversionUtilities.getAsJson(jsonStringWithMissingProperties());
        assertThrows(ConstraintViolationException.class,
                () -> eventExtractor.extract("push", jsonNode));
    }

    @DisplayName("No UrlDisplayText == eventType")
    @Test
    public void givenJsonString_WithNoUrlDisplayText_WhenExtract_ThenUrlDisplayTextIsEventType() {
        JsonNode jsonNode = conversionUtilities.getAsJson(jsonStringWithNoUrlDisplayText());
        GithubEventDto eventDto = eventExtractor.extract("push", jsonNode);

        assertNotNull(eventDto);
        assertEquals("push", eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("http://localhost:abcd", eventDto.url());
        assertEquals("Push Event", eventDto.urlDisplayText());
    }

    @DisplayName("Long UrlDisplayText is Truncated")
    @Test
    public void givenJsonString_WithLongUrlDisplayText_WhenExtract_ThenUrlDisplayTextIsTruncated(){
        JsonNode jsonNode = conversionUtilities.getAsJson(jsonStringWithLongUrlDisplayText());
        GithubEventDto eventDto = eventExtractor.extract("push", jsonNode);

        assertNotNull(eventDto);
        assertEquals("push", eventDto.eventType());
        assertEquals("psyAlexBlakeGoudemond/chatterbox", eventDto.repositoryName());
        assertEquals("psyAlexBlakeGoudemond", eventDto.senderName());
        assertEquals("http://localhost:abcd", eventDto.url());

        assertFalse(eventDto.urlDisplayText().contains("\n"));
        assertTrue(eventDto.urlDisplayText().contains("..."));
    }

    private String jsonStringWithLongUrlDisplayText() {
        String pathToFile = "src/test/resources/payload/github-payload-valid-long-url-display-text.json";
        return conversionUtilities.readPayload(pathToFile);
    }

    private String jsonStringWithNoUrlDisplayText() {
        String pathToFile = "src/test/resources/payload/github-payload-invalid-no-url-display-text.json";
        return conversionUtilities.readPayload(pathToFile);
    }

    private String jsonStringWithMissingProperties() {
        String pathToFile = "src/test/resources/payload/github-payload-invalid-missing-properties.json";
        return conversionUtilities.readPayload(pathToFile);
    }

    private String jsonStringWithUnknownEvent() {
        String pathToFile = "src/test/resources/payload/github-payload-invalid-unknown-event-type.json";
        return conversionUtilities.readPayload(pathToFile);
    }

    private String getValidJsonString() {
        String pathToFile = "src/test/resources/payload/github-payload-valid.json";
        return conversionUtilities.readPayload(pathToFile);
    }

}