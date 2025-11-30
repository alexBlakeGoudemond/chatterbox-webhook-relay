package za.co.psybergate.chatterbox.application.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.psybergate.chatterbox.application.core.utility.EncryptionUtilities;
import za.co.psybergate.chatterbox.application.core.utility.EncryptionUtilitiesImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookMetrics;
import za.co.psybergate.chatterbox.infrastructure.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    @Value("${webhook.github.payload}")
    private String webhookPayload;

    @MockitoBean
    private WebhookMetrics webhookMetrics;  // Mocked so Spring can inject it

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EncryptionUtilities encryptionUtilities;

    @DisplayName("Missing signature fails")
    @Test
    void whenPostToGithubWebhook_WithJsonAndNoSignature_ThenFailure() {
        try {
            mockMvc.perform(post(apiPrefix + "/webhook/github")
                    .contentType(APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(webhookPayload)
                    .header("X-GitHub-Delivery", "123")
                    .header("X-GitHub-Event", "push")
            );
        } catch (Exception expected) {
            assertTrue(expected.getMessage().contains("Missing X-Hub-Signature-256"));
            return;
        }
        fail("Expected an exception to be thrown due to a Missing Signature");
    }

    @DisplayName("Invalid signature fails")
    @Test
    void whenPostToGithubWebhook_WithJsonAndInvalidSignature_ThenFailure() {
        try {
            mockMvc.perform(post(apiPrefix + "/webhook/github")
                    .contentType(APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(webhookPayload)
                    .header("X-GitHub-Delivery", "123")
                    .header("X-GitHub-Event", "push")
                    .header("X-Hub-Signature-256", webhookSecret)
            );
        } catch (Exception expected) {
            assertTrue(expected.getMessage().contains("Invalid X-Hub-Signature-256"));
            return;
        }
        fail("Expected an exception to be thrown due to an Invalid Signature");
    }

    @DisplayName("Encrypted signature succeeds")
    @Test
    void whenPostToGithubWebhook_WithJson_ThenAccepted() throws Exception {
        String encryptedSignature = encryptionUtilities.encryptUsingSHA256(webhookSecret, webhookPayload);

        mockMvc.perform(post(apiPrefix + "/webhook/github")
                        .contentType(APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(webhookPayload)
                        .header("X-GitHub-Delivery", "123")
                        .header("X-GitHub-Event", "push")
                        .header("X-Hub-Signature-256", encryptedSignature)
                )
                .andExpect(status().isAccepted());
    }

}