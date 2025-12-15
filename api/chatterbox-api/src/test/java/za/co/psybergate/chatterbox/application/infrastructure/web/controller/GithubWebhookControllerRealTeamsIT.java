package za.co.psybergate.chatterbox.application.infrastructure.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
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
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverter;
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

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/// Similar to [GithubWebhookControllerMockedTeamsIT] however this one calls
/// The Real MS Teams
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
@ActiveProfiles({"test", "live-url"})
public class GithubWebhookControllerRealTeamsIT {

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
    private JsonConverter jsonConverter;

    /// Send an actual test to the MS Teams API and assert that the HttpResponse
    /// information is as-expected.
    ///
    /// This test is annotated with a Tag that `maven-surefire-plugin` is made aware of.
    /// This means that running `mvn clean install` will NOT include this by default
    @Tag("live-integration")
    @DisplayName("Sending to Live MS Teams: ACCEPTED")
    @Test
    void whenPostToGithubWebhook_WithJsonAndValidSignature_ThenHttpStatusAccepted() {
        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(securityWebhookGithubProperties.getSecret(), readGithubPayload());
        try {
            String expectedContentBody = "Webhook received; work underway";
            mockMvc.perform(httpRequest)
                    .andExpect(status().isAccepted())
                    .andExpect(content().string(expectedContentBody));
        } catch (Exception e) {
            fail("Expected the HttpRequest to succeed without an exception", e);
        }
    }

    private MockHttpServletRequestBuilder getHttpRequestValid(String payloadSecret, String payload) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(payloadSecret, payload);
        return post(chatterboxApiProperties.getPrefix() + "/webhook/github")
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