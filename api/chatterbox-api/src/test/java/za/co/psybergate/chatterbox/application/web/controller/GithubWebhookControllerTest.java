package za.co.psybergate.chatterbox.application.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import za.co.psybergate.chatterbox.application.core.utility.EncryptionUtilities;
import za.co.psybergate.chatterbox.application.core.utility.EncryptionUtilitiesImpl;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubWebhookController.class)
@Import(EncryptionUtilitiesImpl.class)
public class GithubWebhookControllerTest {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    @Value("${webhook.github.payload}")
    private String webhookPayload;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EncryptionUtilities encryptionUtilities;

    @DisplayName("Missing signature fails")
    @Test
    void whenPostToGithubWebhook_WithJsonAndNoSignature_ThenFailure() throws Exception {
        mockMvc.perform(post(apiPrefix + "/webhook/github")
                        .contentType(APPLICATION_JSON)
                        .content(webhookPayload))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Invalid signature fails")
    @Test
    void whenPostToGithubWebhook_WithJsonAndInvalidSignature_ThenFailure() throws Exception {
        mockMvc.perform(post(apiPrefix + "/webhook/github")
                        .contentType(APPLICATION_JSON)
                        .content(webhookPayload)
                        .header("X-Hub-Signature-256", webhookSecret))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Encrypted signature succeeds")
    @Test
    void whenPostToGithubWebhook_WithJson_ThenAccepted() throws Exception {
        String encryptedSignature = encryptionUtilities.encryptUsingSHA256(webhookSecret, webhookPayload);

        mockMvc.perform(post(apiPrefix + "/webhook/github")
                        .contentType(APPLICATION_JSON)
                        .content(webhookPayload)
                        .header("X-Hub-Signature-256", encryptedSignature))
                .andExpect(status().isAccepted());
    }

}