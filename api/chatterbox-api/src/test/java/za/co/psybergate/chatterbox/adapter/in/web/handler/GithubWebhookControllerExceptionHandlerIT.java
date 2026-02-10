package za.co.psybergate.chatterbox.adapter.in.web.handler;

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
import za.co.psybergate.architecture_rules.quality.MirrorProductionClassForArchitectureRuleTests;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.validation.GithubWebhookValidator;
import za.co.psybergate.chatterbox.adapter.in.web.controller.GithubWebhookController;
import za.co.psybergate.chatterbox.adapter.out.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.adapter.out.teams.delivery.TeamsWebhookSender;
import za.co.psybergate.chatterbox.adapter.out.teams.factory.TeamsAdaptiveCardFactory;
import za.co.psybergate.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.common.template.RegexTemplateSubstitutor;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.common.logging.convenience.ImportSlf4jWebhookLogger;
import za.co.psybergate.chatterbox.common.exception.InfrastructureException;
import za.co.psybergate.chatterbox.common.logging.mdc.Slf4jMdcContext;
import za.co.psybergate.chatterbox.common.security.HmacSha256Cryptor;
import za.co.psybergate.chatterbox.test.helper.GithubHttpRequestFactory;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportSlf4jWebhookLogger
@Import({
        HmacSha256Cryptor.class,
        InfrastructurePropertiesConfig.class,
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
@MirrorProductionClassForArchitectureRuleTests(GlobalExceptionHandler.class)
public class GithubWebhookControllerExceptionHandlerIT {

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubHttpRequestFactory githubHttpRequestFactory;

    @MockitoBean
    @Qualifier("webhookOrchestrator")
    private WebhookOrchestratorPort webhookOrchestrator;

    @DisplayName("ConstraintViolationException -> BAD_REQUEST")
    @Test
    public void whenServiceRaisesConstraintViolationException_ThenHandlerProducesBadRequest() {
        mock_WebhookOrchestrator_Process_ToThrow(ConstraintViolationException.class);

        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValid(jsonFileReader.getGithubPayloadValidAsString());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
        }
    }

    @DisplayName("ApplicationException -> BAD_REQUEST")
    @Test
    public void whenServiceRaisesApplicationException_ThenHandlerProducesBadRequest() {
        mock_WebhookOrchestrator_Process_ToThrow(ApplicationException.class);

        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValid(jsonFileReader.getGithubPayloadValidAsString());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
        }
    }

    @DisplayName("Infrastructure Exception -> INTERNAL_SERVER_ERROR")
    @Test
    public void whenServiceRaisesInfrastructureException_ThenHandlerProducesInternalServerError() {
        mock_WebhookOrchestrator_Process_ToThrow(InfrastructureException.class);

        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValid(jsonFileReader.getGithubPayloadValidAsString());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
        }
    }

    @DisplayName("External Exception -> INTERNAL_SERVER_ERROR")
    @Test
    public void whenServiceRaisesExternalException_ThenHandlerProducesInternalServerError() {
        mock_WebhookOrchestrator_Process_ToThrow(RuntimeException.class);

        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValid(jsonFileReader.getGithubPayloadValidAsString());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
        }
    }

    private void mock_WebhookOrchestrator_Process_ToThrow(Class<? extends Throwable> exceptionClass) {
        Mockito.doThrow(exceptionClass)
                .when(webhookOrchestrator).process(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

}
