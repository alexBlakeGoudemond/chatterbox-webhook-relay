package com.webhook.relay.chatterbox.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.architecture_rules.quality.MirrorProductionClassForArchitectureRuleTests;
import com.webhook.relay.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import com.webhook.relay.chatterbox.adapter.in.validation.GithubWebhookValidator;
import com.webhook.relay.chatterbox.adapter.in.web.filter.WebhookFilter;
import com.webhook.relay.chatterbox.adapter.out.http.HttpResponseHandler;
import com.webhook.relay.chatterbox.adapter.out.teams.delivery.TeamsWebhookSender;
import com.webhook.relay.chatterbox.adapter.out.teams.factory.TeamsAdaptiveCardFactory;
import com.webhook.relay.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import com.webhook.relay.chatterbox.adapter.out.webhook.poll.GithubRestPollingClient;
import com.webhook.relay.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import com.webhook.relay.chatterbox.application.common.template.RegexTemplateSubstitutor;
import com.webhook.relay.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventReceived;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import com.webhook.relay.chatterbox.application.usecase.webhook.orchestration.WebhookOrchestrator;
import com.webhook.relay.chatterbox.common.config.InfrastructurePropertiesConfig;
import com.webhook.relay.chatterbox.common.logging.convenience.ImportSlf4jWebhookLogger;
import com.webhook.relay.chatterbox.common.logging.mdc.Slf4jMdcContext;
import com.webhook.relay.chatterbox.common.security.HmacSha256Cryptor;
import com.webhook.relay.chatterbox.test.helper.GithubHttpRequestFactory;
import com.webhook.relay.chatterbox.test.helper.JsonFileReader;

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
@ImportSlf4jWebhookLogger
@Import({
        WebhookFilter.class,
        HmacSha256Cryptor.class,
        InfrastructurePropertiesConfig.class,
        WebhookOrchestrator.class,
        GithubWebhookValidator.class,
        PropertiesConfigurationResolver.class,
        GithubWebhookEventMapper.class,
        JsonFileReader.class,
        JacksonJsonConverter.class,
        TeamsWebhookSender.class,
        TeamsAdaptiveCardFactory.class,
        RegexTemplateSubstitutor.class,
        GithubHttpRequestFactory.class,
        HttpResponseHandler.class,
        Slf4jMdcContext.class,
})
@WebMvcTest(GithubWebhookController.class)
@MirrorProductionClassForArchitectureRuleTests(GithubWebhookController.class)
@ExtendWith(MockitoExtension.class)
public class GithubWebhookControllerMockedTeamsIT {

    @MockitoBean
    private GithubRestPollingClient githubPollingService;

    @MockitoBean
    private WebhookEventStorePort webhookEventStorePort;

    @MockitoBean
    private WebhookPolledEventStorePort webhookPolledEventStorePort;

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
                    .andExpect(status().isBadRequest())
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
        Mockito.when(webhookEventStorePort.storeWebhook(Mockito.anyString(), Mockito.any(OutboundEvent.class), Mockito.any(RawEventPayload.class)))
                .thenReturn(mockedWebhookEventReceived());
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
        Mockito.when(webhookEventStorePort.storeWebhook(Mockito.anyString(), Mockito.any(OutboundEvent.class), Mockito.any(RawEventPayload.class)))
                .thenReturn(mockedWebhookEventReceived());
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

    private WebhookEventReceived mockedWebhookEventReceived() {
        return new WebhookEventReceived(0L,
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

}