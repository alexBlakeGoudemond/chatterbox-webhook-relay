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

    // TODO BlakeGoudemond 2025/12/06 | write test where extractor.read has dotPath of null (not defined in properties?)
    // TODO BlakeGoudemond 2025/12/06 | ffup what isValueNode() represents and how to write test for it

    @Autowired
    private ConversionUtilities conversionUtilities;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;  // Mocked so Spring can inject it

    @DisplayName("Extractor maps to DTO")
    @Test
    public void givenJsonString_WhenExtract_ThenSuccess() {
        String jsonString = getValidJsonString();
        JsonNode asJson = conversionUtilities.getAsJson(jsonString);
        GithubEventDto eventDto = eventExtractor.extract("push", asJson);

        assertNotNull(eventDto);
        assertEquals("push", eventDto.eventType());
        assertEquals("ChuckNorris", eventDto.repositoryName());
        assertEquals("wildWest", eventDto.senderName());
        assertEquals("http://localhost:abcd", eventDto.url());
        assertEquals("dummy push event", eventDto.urlDisplayText());
    }

    @DisplayName("Unknown Event: FORBIDDEN")
    @Test
    public void givenJsonString_WithUnknownEvent_WhenExtract_ThenForbidden() {
        String jsonStringWithUnknownEvent = """
                        {
                            "eventType": "unknownEvent"
                        }
                """;
        JsonNode asJson = conversionUtilities.getAsJson(jsonStringWithUnknownEvent);
        assertThrows(UnrecognizedRequestException.class,
                () -> eventExtractor.extract("unknownEvent", asJson));
    }

    @DisplayName("Missing JSON keys: Exception")
    @Test
    public void givenIncompleteJsonString_WhenExtract_ThenFailure() {
        String jsonStringWithMissingProperties = """
                    {
                        "eventType": "push"
                    }
                """;
        JsonNode asJson = conversionUtilities.getAsJson(jsonStringWithMissingProperties);
        assertThrows(ConstraintViolationException.class,
                () -> eventExtractor.extract("push", asJson));
    }

    private String getValidJsonString() {
        return """
                    {
                        "eventType": "push",
                        "repository": {
                            "full_name": "ChuckNorris"
                        },
                        "sender": {
                            "login": "wildWest"
                        },
                        "issue": {
                            "html_url": "http://localhost:abcd"
                        },
                        "head_commit": {
                            "message": "dummy push event"
                        }
                    }
                """;
    }

}