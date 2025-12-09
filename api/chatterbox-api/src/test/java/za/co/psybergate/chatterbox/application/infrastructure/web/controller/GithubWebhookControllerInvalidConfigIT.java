package za.co.psybergate.chatterbox.application.infrastructure.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.chatterbox.application.webhook.extractor.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.application.webhook.extractor.resolver.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.application.webhook.service.GithubWebhookServiceImpl;
import za.co.psybergate.chatterbox.application.teams.sending.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.application.teams.factory.template.TeamsTemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.webhook.validator.WebhookRequestValidatorImpl;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;
import za.co.psybergate.chatterbox.domain.utility.JsonConverterImpl;
import za.co.psybergate.chatterbox.domain.utility.PayloadCryptor;
import za.co.psybergate.chatterbox.domain.utility.PayloadCryptorImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.web.controller.GithubWebhookController;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@ActiveProfiles({"bad-properties"})
public class GithubWebhookControllerInvalidConfigIT {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;  // Mocked so Spring can inject it

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PayloadCryptor payloadCryptor;

    @Autowired
    private JsonConverter jsonConverter;

    /// One of the properties that the codebase expects is a field `urlDisplayText`.
    /// This test uses a properties file where this field is NOT included; the assertion is that
    /// a 500 Http StatusCode is produced
    @DisplayName("Invalid Properties: INTERNAL SERVER ERROR")
    @Test
    void whenPostToGithubWebhook_WithInvalidProperties_ThenInternalServerError() {
        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret, readGithubPayload());
        try {
            String expectedContentBody = "extract.<return value>.senderName: must not be null";
            mockMvc.perform(httpRequest)
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an exception", e);
        }
    }

    private MockHttpServletRequestBuilder getHttpRequestValid(String payloadSecret, String payload) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(payloadSecret, payload);
        return post(apiPrefix + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", encryptedSignature);
    }

    private String readGithubPayload() {
        String pathToFile = "src/test/resources/payload/github-payload-valid.json";
        return jsonConverter.readPayload(pathToFile);
    }

}
