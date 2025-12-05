package za.co.psybergate.chatterbox.application.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.chatterbox.application.core.utility.EncryptionUtilities;
import za.co.psybergate.chatterbox.application.core.utility.EncryptionUtilitiesImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.exception.ApplicationException;
import za.co.psybergate.chatterbox.infrastructure.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

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
///        │ 2. Reads raw body       │              - EncryptionUtilities
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
///        (e.g., .andExpect(status().isAccepted()))
/// ```
@Import({
        WebhookFilter.class,
        WebhookLogger.class,
        EncryptionUtilitiesImpl.class,
})
@WebMvcTest(GithubWebhookController.class)
public class GithubWebhookControllerIT {

    // TODO BlakeGoudemond 2025/12/04 | tests when missingConfigFile
    // TODO BlakeGoudemond 2025/12/04 | tests when config does not have the right properties
    // TODO BlakeGoudemond 2025/12/04 | tests when url does not have the right properties

    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    @Value("${webhook.github.payload}")
    private String webhookPayload;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;  // Mocked so Spring can inject it

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EncryptionUtilities encryptionUtilities;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Unaccepted Repository: OK")
    @Test
    public void givenValidPayload_AndUnacceptedRepositoryName_ThenHttpStatusOk() {
        String unrecognizedRepositoryName = "unknownOwner/unknownRepository";

        JsonNode jsonNode = getAsJson(webhookPayload);
        if (!(jsonNode instanceof ObjectNode)) {
            fail("Unable to mutate JsonNode - needed for the test to change the RepositoryName");
        }
        ObjectNode nodeWithContentToReplace = (ObjectNode) jsonNode.get("repository");
        nodeWithContentToReplace.put("full_name", unrecognizedRepositoryName);

        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret, jsonNode.toString());
        try {
            String expectedContentBody =
                    String.format("Webhook received; no work done; unrecognized repository \"%s\"", unrecognizedRepositoryName);
            mockMvc.perform(httpRequest)
                    .andExpect(status().isOk())
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
        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret, webhookPayload);
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
        return post(apiPrefix + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(webhookPayload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push");
    }

    private MockHttpServletRequestBuilder getHttpRequestInvalidSignature() {
        return post(apiPrefix + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(webhookPayload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", webhookSecret);
    }

    private MockHttpServletRequestBuilder getHttpRequestValid(String payloadSecret, String payload) {
        String encryptedSignature = encryptionUtilities.encryptUsingSHA256(payloadSecret, payload);
        return post(apiPrefix + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", encryptedSignature);
    }

    private JsonNode getAsJson(String jsonString) throws ApplicationException {
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Unable to convert String into JSON", e);
        }
    }

}