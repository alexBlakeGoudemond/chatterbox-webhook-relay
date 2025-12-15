package za.co.psybergate.chatterbox.application.infrastructure.web.handler;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.application.teams.factory.template.TeamsTemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.webhook.ingest.WebhookRequestValidatorImpl;
import za.co.psybergate.chatterbox.application.webhook.orchestration.GithubWebhookService;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;
import za.co.psybergate.chatterbox.domain.utility.JsonConverterImpl;
import za.co.psybergate.chatterbox.domain.utility.PayloadCryptor;
import za.co.psybergate.chatterbox.domain.utility.PayloadCryptorImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxApiProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSecurityWebhookGithubProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.BadRequestException;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.web.controller.GithubWebhookController;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        WebhookFilter.class,
        WebhookLogger.class,
        PayloadCryptorImpl.class,
        ApplicationConfig.class,
        WebhookRequestValidatorImpl.class,
        WebhookConfigurationResolverImpl.class,
        GithubEventExtractorImpl.class,
        JsonConverterImpl.class,
        TeamsSenderServiceImpl.class,
        TeamsCardFactoryImpl.class,
        TeamsTemplateSubstitutorImpl.class,
})
@WebMvcTest(GithubWebhookController.class)
public class GithubWebhookControllerExceptionHandlerIT {

    @Autowired
    private ChatterboxApiProperties chatterboxApiProperties;

    @Autowired
    private ChatterboxSecurityWebhookGithubProperties securityWebhookGithubProperties;    

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PayloadCryptor payloadCryptor;

    @Autowired
    private JsonConverter jsonConverter;

    @MockitoBean
    @Qualifier("githubWebhookServiceImpl")
    private GithubWebhookService githubWebhookService;

    @DisplayName("InternalServerException -> INTERNAL_SERVER_ERROR")
    @Test
    public void whenServiceRaisesInternalServerException_ThenHandlerProducesInternalServerError() {
        Mockito.doThrow(InternalServerException.class)
                .when(githubWebhookService).process(Mockito.anyString(), Mockito.any(JsonNode.class));

        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret(), readGithubPayload());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
        }
    }

    @DisplayName("UnrecognizedRequestException -> FORBIDDEN")
    @Test
    public void whenServiceRaisesUnrecognizedRequestException_ThenHandlerProducesForbidden() {
        Mockito.doThrow(UnrecognizedRequestException.class)
                .when(githubWebhookService).process(Mockito.anyString(), Mockito.any(JsonNode.class));

        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret(), readGithubPayload());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isForbidden());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
        }
    }

    @DisplayName("ConstraintViolationException -> INTERNAL_SERVER_ERROR")
    @Test
    public void whenServiceRaisesConstraintViolationException_ThenHandlerProducesInternalServerError() {
        Mockito.doThrow(ConstraintViolationException.class)
                .when(githubWebhookService).process(Mockito.anyString(), Mockito.any(JsonNode.class));

        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret(), readGithubPayload());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
        }
    }

    @DisplayName("BadRequestException -> BAD_REQUEST")
    @Test
    public void whenServiceRaisesBadRequestException_ThenHandlerProducesBadRequest() {
        Mockito.doThrow(BadRequestException.class)
                .when(githubWebhookService).process(Mockito.anyString(), Mockito.any(JsonNode.class));

        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret(), readGithubPayload());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
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

    private String webhookSecret() {
        return securityWebhookGithubProperties.getSecret();
    }

    private String readGithubPayload() {
        String pathToFile = "src/test/resources/payload/github-payload-valid.json";
        return jsonConverter.readPayload(pathToFile);
    }

}
