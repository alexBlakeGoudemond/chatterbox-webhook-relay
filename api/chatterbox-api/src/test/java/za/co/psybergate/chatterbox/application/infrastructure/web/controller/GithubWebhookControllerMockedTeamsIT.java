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
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.application.teams.factory.template.TeamsTemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.webhook.ingest.WebhookRequestValidatorImpl;
import za.co.psybergate.chatterbox.application.webhook.orchestration.GithubWebhookServiceImpl;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.helper.JsonFileReader;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.application.webhook.security.PayloadCryptor;
import za.co.psybergate.chatterbox.application.webhook.security.PayloadCryptorImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxApiProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSecurityWebhookGithubProperties;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.web.controller.GithubWebhookController;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        WebhookLogger.class,
        PayloadCryptorImpl.class,
        ApplicationConfig.class,
        GithubWebhookServiceImpl.class,
        WebhookRequestValidatorImpl.class,
        WebhookConfigurationResolverImpl.class,
        GithubEventExtractorImpl.class,
        JsonConverterImpl.class,
        TeamsSenderServiceImpl.class,
        TeamsCardFactoryImpl.class,
        TeamsTemplateSubstitutorImpl.class,
})
@WebMvcTest(GithubWebhookController.class)
public class GithubWebhookControllerMockedTeamsIT {
    
    @Autowired
    private ChatterboxApiProperties chatterboxApiProperties;

    @Autowired
    private ChatterboxSecurityWebhookGithubProperties securityWebhookGithubProperties;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;  // Mocked so Spring can inject it

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PayloadCryptor payloadCryptor;

    @Autowired
    private JsonFileReader jsonFileReader;

    @DisplayName("Missing JSON properties: BAD_REQUEST")
    @Test
    public void givenIncompletePayload_MissingJsonProperties_ThenHttpStatusOk() {
        String incompletePayload = "{}";
        MockHttpServletRequestBuilder httpRequest = getHttpRequestValidNoEncoding(webhookSecret(), incompletePayload);
        try {
            String expectedContentBody = "Unable to parse 'repository.full_name' from raw rawBody";
            mockMvc.perform(httpRequest)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an exception", e);
        }
    }

    @DisplayName("Unrecognized event: FORBIDDEN")
    @Test
    public void givenValidPayload_AndUnacceptedEventType_ThenHttpStatusOk() {
        String unknownEventType = "strangeEvent";
        MockHttpServletRequestBuilder httpRequest = getHttpRequestUnknownEvent(webhookSecret(), jsonFileReader.getGithubPayloadValidAsString(), unknownEventType);
        try {
            String responseContent =
                    String.format("Webhook received; no work done; unrecognized event '%s'", unknownEventType);
            mockMvc.perform(httpRequest)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(responseContent));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an Exception", e);
        }
    }

    @DisplayName("Unrecognized Repository: FORBIDDEN")
    @Test
    public void givenValidPayload_AndUnacceptedRepositoryName_ThenHttpStatusOk() {
        String unrecognizedRepositoryName = "unknownOwner/unknownRepository";

        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        if (!(jsonNode instanceof ObjectNode)) {
            fail("Unable to mutate JsonNode - needed for the test to change the RepositoryName");
        }
        ObjectNode nodeWithContentToReplace = (ObjectNode) jsonNode.get("repository");
        nodeWithContentToReplace.put("full_name", unrecognizedRepositoryName);

        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret(), jsonNode.toString());
        try {
            String expectedContentBody =
                    String.format("Webhook received; no work done; unrecognized repository '%s'", unrecognizedRepositoryName);
            mockMvc.perform(httpRequest)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an Exception", e);
        }
    }

    @DisplayName("Missing signature fails")
    @Test
    void whenPostToGithubWebhook_WithJsonAndNoSignature_ThenExceptionThrown() {
        MockHttpServletRequestBuilder httpRequestNoSignature = getHttpRequestNoSignature();
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
    void whenPostToGithubWebhook_WithJsonAndInvalidSignature_ThenExceptionThrown() {
        MockHttpServletRequestBuilder httpRequestInvalidSignature = getHttpRequestInvalidSignature();
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
    void whenPostToGithubWebhook_WithJsonAndValidSignature_ThenHttpStatusAccepted() {
        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret(), jsonFileReader.getGithubPayloadValidAsString());
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
    void whenPostToGithubWebhook_WithValidPayload_AndNoEncoding_ThenHttpStatusAccepted() {
        MockHttpServletRequestBuilder httpRequest = getHttpRequestValidNoEncoding(webhookSecret(), jsonFileReader.getGithubPayloadValidAsString());
        try {
            String expectedContentBody = "Webhook received; work underway";
            mockMvc.perform(httpRequest)
                    .andExpect(status().isAccepted())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an exception", e);
        }
    }

    private MockHttpServletRequestBuilder getHttpRequestNoSignature() {
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonFileReader.getGithubPayloadValidAsString())
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push");
    }

    private String apiPrefix() {
        return chatterboxApiProperties.getPrefix();
    }

    private String webhookSecret() {
        return securityWebhookGithubProperties.getSecret();
    }

    private MockHttpServletRequestBuilder getHttpRequestInvalidSignature() {
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(jsonFileReader.getGithubPayloadValidAsString())
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", webhookSecret());
    }

    private MockHttpServletRequestBuilder getHttpRequestUnknownEvent(String payloadSecret, String payload, String unknownEventType) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(payloadSecret, payload);
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", unknownEventType)
                .header("X-Hub-Signature-256", encryptedSignature);
    }

    private MockHttpServletRequestBuilder getHttpRequestValidNoEncoding(String payloadSecret, String payload) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(payloadSecret, payload);
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", encryptedSignature);
    }

    private MockHttpServletRequestBuilder getHttpRequestValid(String payloadSecret, String payload) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(payloadSecret, payload);
        return post(apiPrefix() + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", encryptedSignature);
    }

}