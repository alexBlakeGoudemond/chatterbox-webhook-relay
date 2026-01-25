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
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.GithubWebhookPort;
import za.co.psybergate.chatterbox.application.usecase.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.application.usecase.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.usecase.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapperImpl;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.adapter.webhook.validation.WebhookRequestValidatorImpl;
import za.co.psybergate.chatterbox.infrastructure.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.infrastructure.common.exception.InfrastructureException;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.web.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.web.controller.GithubWebhookController;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.web.security.PayloadCryptorImpl;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.webhook.resolution.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.test.helper.GithubHttpRequestFactory;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        WebhookFilter.class,
        WebhookLoggerImpl.class,
        PayloadCryptorImpl.class,
        InfrastructurePropertiesConfig.class,
        WebhookRequestValidatorImpl.class,
        WebhookConfigurationResolverImpl.class,
        GithubEventMapperImpl.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        TeamsSenderServiceImpl.class,
        TeamsCardFactoryImpl.class,
        TemplateSubstitutorImpl.class,
        GithubHttpRequestFactory.class,
        HttpResponseHandler.class
})
@WebMvcTest(GithubWebhookController.class)
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
    @Qualifier("githubWebhookServiceImpl")
    private GithubWebhookPort githubWebhookPort;

    @DisplayName("ConstraintViolationException -> BAD_REQUEST")
    @Test
    public void whenServiceRaisesConstraintViolationException_ThenHandlerProducesBadRequest() {
        Mockito.doThrow(ConstraintViolationException.class)
                .when(githubWebhookPort).process(Mockito.anyString(), Mockito.anyString(), Mockito.any(JsonNode.class));

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
        Mockito.doThrow(ApplicationException.class)
                .when(githubWebhookPort).process(Mockito.anyString(), Mockito.anyString(), Mockito.any(JsonNode.class));

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
        Mockito.doThrow(InfrastructureException.class)
                .when(githubWebhookPort).process(Mockito.anyString(), Mockito.anyString(), Mockito.any(JsonNode.class));

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
        Mockito.doThrow(RuntimeException.class)
                .when(githubWebhookPort).process(Mockito.anyString(), Mockito.anyString(), Mockito.any(JsonNode.class));

        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValid(jsonFileReader.getGithubPayloadValidAsString());
        try {
            mockMvc.perform(httpRequest)
                    .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            fail("Expected the HttpRequest to produce the expected HttpRequest", e);
        }
    }

}
