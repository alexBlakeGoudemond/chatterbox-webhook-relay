package za.co.psybergate.chatterbox.application.webhook.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@SpringBootTest(classes = {
        GithubEventExtractor.class,
        ApplicationConfig.class,
        ConversionUtilitiesImpl.class,
        WebhookFilter.class,
        WebhookLogger.class,
        EncryptionUtilitiesImpl.class,
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
        String jsonString = """
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
        String jsonString = """
                        {
                            "eventType": "unknownEvent"
                        }
                """;
        JsonNode asJson = conversionUtilities.getAsJson(jsonString);
        try {
            eventExtractor.extract("unknownEvent", asJson);
        } catch (UnrecognizedRequestException exception) {
            return;
        }
        fail("Expected exception to be thrown for unknownEvent");
    }

}