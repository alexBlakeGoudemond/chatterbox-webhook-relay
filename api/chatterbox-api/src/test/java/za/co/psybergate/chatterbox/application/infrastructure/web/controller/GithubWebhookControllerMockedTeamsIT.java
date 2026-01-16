package za.co.psybergate.chatterbox.application.infrastructure.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.chatterbox.application.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledEventStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookEventStore;
import za.co.psybergate.chatterbox.application.webhook.orchestration.GithubWebhookServiceImpl;
import za.co.psybergate.chatterbox.application.webhook.security.PayloadCryptorImpl;
import za.co.psybergate.chatterbox.infrastructure.web.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.github.delivery.GithubPollingServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.application.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.infrastructure.web.controller.GithubWebhookController;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.webhook.validation.WebhookRequestValidatorImpl;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.infrastructure.webhook.resolution.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.test.helper.GithubHttpRequestFactory;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/// Integration test that navigates through the [WebhookFilter]
/// and the [GithubWebhookController].
///
/// The Imports and Mocks work because of this diagram:
///
/// ```java
///        [Test code: mockMvc.perform()]
///                  │
///                  ▼
///        ┌─────────────────────────┐
///        │ MockMvc Dispatcher      │
///        └─────────┬───────────────┘
///                  │
///        Applies registered filters (WebhookFilter)
///                  │
///                  ▼
///        ┌─────────────────────────┐
///        │   WebhookFilter         │ ───────────> Depends on:
///        ├─────────────────────────┤              - WebhookLogger
///        │ 1. Reads headers        │                (Imported)
///        │ 2. Reads raw rawBody       │              - EncryptionUtilities
///        │ 3. Validates sig        │                (Imported; implementation)
///        │ 4. Logs events          │              - WebhookMetrics
///        │ 5. Calls metrics        │                (Mocked)
///        └─────────┬───────────────┘
///                  │
///        If signature valid
///                  ▼
///        ┌─────────────────────────┐
///        │ GithubWebhookController │
///        │  Handles the webhook    │
///        └─────────┬───────────────┘
///                  │
///                  ▼
///        Response generated (e.g., 202 ACCEPTED)
///                  │
///                  ▼
///        MockMvc captures response
///                  │
///                  ▼
///        Back to test assertion
///        (e.g., .andExpect(httpStatus().isAccepted()))
/// ```
@Import({
        WebhookFilter.class,
        WebhookLoggerImpl.class,
        PayloadCryptorImpl.class,
        ApplicationConfig.class,
        GithubWebhookServiceImpl.class,
        WebhookRequestValidatorImpl.class,
        WebhookConfigurationResolverImpl.class,
        GithubEventExtractorImpl.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        TeamsSenderServiceImpl.class,
        TeamsCardFactoryImpl.class,
        TemplateSubstitutorImpl.class,
        GithubHttpRequestFactory.class,
        HttpResponseHandler.class,
})
@WebMvcTest(GithubWebhookController.class)
public class GithubWebhookControllerMockedTeamsIT {

    @MockitoBean
    private GithubPollingServiceImpl githubPollingService;

    @MockitoBean
    private WebhookEventStore webhookEventStore;

    @MockitoBean
    private GithubPolledEventStore githubPolledEventStore;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;  // Mocked so Spring can inject it

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubHttpRequestFactory githubHttpRequestFactory;

    @DisplayName("Missing JSON properties: EXCEPTION")
    @Test
    public void givenIncompletePayload_MissingJsonProperties_ThenHttpStatusOk() {
        String incompletePayload = "{}";
        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValidNoEncoding(incompletePayload);
        try {
            String expectedContentBody = "Unable to parse 'repository.full_name' from raw rawBody";
            mockMvc.perform(httpRequest)
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an exception", e);
        }
    }

    @DisplayName("Unrecognized event: BAD_REQUEST")
    @Test
    public void givenValidPayload_AndUnacceptedEventType_ThenBadRequest() {
        String unknownEventType = "strangeEvent";
        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestUnknownEvent(jsonFileReader.getGithubPayloadValidAsString(), unknownEventType);
        try {
            String responseContent =
                    String.format("Webhook received; no work done; unrecognized event '%s'", unknownEventType);
            mockMvc.perform(httpRequest)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(responseContent));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an Exception", e);
        }
    }

    @DisplayName("Unrecognized Repository: BAD_REQUEST")
    @Test
    public void givenValidPayload_AndUnacceptedRepositoryName_ThenBadRequest() {
        String unrecognizedRepositoryName = "unknownOwner/unknownRepository";

        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        if (!(jsonNode instanceof ObjectNode)) {
            fail("Unable to mutate JsonNode - needed for the test to change the RepositoryName");
        }
        ObjectNode nodeWithContentToReplace = (ObjectNode) jsonNode.get("repository");
        nodeWithContentToReplace.put("full_name", unrecognizedRepositoryName);

        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValid(jsonNode.toString());
        try {
            String expectedContentBody =
                    String.format("Webhook received; no work done; unrecognized repository '%s'", unrecognizedRepositoryName);
            mockMvc.perform(httpRequest)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an Exception", e);
        }
    }

    @DisplayName("Missing signature fails")
    @Test
    public void whenPostToGithubWebhook_WithJsonAndNoSignature_ThenExceptionThrown() {
        MockHttpServletRequestBuilder httpRequestNoSignature = githubHttpRequestFactory.getHttpRequestNoSignature(jsonFileReader.getGithubPayloadValidAsString());
        try {
            mockMvc.perform(httpRequestNoSignature);
        } catch (Exception expected) {
            assertTrue(expected.getMessage().contains("Missing X-Hub-Signature-256"));
            return;
        }
        fail("Expected an exception to be thrown due to a Missing Signature");
    }

    @DisplayName("Invalid signature fails")
    @Test
    public void whenPostToGithubWebhook_WithJsonAndInvalidSignature_ThenExceptionThrown() {
        MockHttpServletRequestBuilder httpRequestInvalidSignature = githubHttpRequestFactory.getHttpRequestInvalidSignature(jsonFileReader.getGithubPayloadValidAsString());
        try {
            mockMvc.perform(httpRequestInvalidSignature);
        } catch (Exception expected) {
            assertTrue(expected.getMessage().contains("Invalid X-Hub-Signature-256"));
            return;
        }
        fail("Expected an exception to be thrown due to an Invalid Signature");
    }

    @DisplayName("Encrypted signature: ACCEPTED")
    @Test
    public void whenPostToGithubWebhook_WithJsonAndValidSignature_ThenHttpStatusAccepted() {
        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValid(jsonFileReader.getGithubPayloadValidAsString());
        try {
            String expectedContentBody = "Webhook received; work underway";
            mockMvc.perform(httpRequest)
                    .andExpect(status().isAccepted())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an exception", e);
        }
    }

    @DisplayName("Signature, No UTF-8: ACCEPTED")
    @Test
    public void whenPostToGithubWebhook_WithValidPayload_AndNoEncoding_ThenHttpStatusAccepted() {
        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValidNoEncoding(jsonFileReader.getGithubPayloadValidAsString());
        try {
            String expectedContentBody = "Webhook received; work underway";
            mockMvc.perform(httpRequest)
                    .andExpect(status().isAccepted())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an exception", e);
        }
    }

}