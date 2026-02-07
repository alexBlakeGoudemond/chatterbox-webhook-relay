package za.co.psybergate.chatterbox.adapter.in.web.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.chatterbox.common.convenience.annotation.logging.ImportSlf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;
import za.co.psybergate.chatterbox.application.common.logging.slf4j.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.adapter.in.validation.GithubWebhookValidator;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.common.exception.InvalidSignatureException;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.common.security.HmacSha256Cryptor;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.test.helper.GithubHttpRequestFactory;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import static org.junit.jupiter.api.Assertions.*;

@ImportSlf4jWebhookLogger
@SpringBootTest(classes = {
        WebhookFilter.class,
        Slf4jWebhookLogger.class,
        HmacSha256Cryptor.class,
        InfrastructurePropertiesConfig.class,
        GithubWebhookValidator.class,
        PropertiesConfigurationResolver.class,
        JsonFileReader.class,
        JacksonJsonConverter.class,
        WebhookRuntimeMetrics.class,
        SimpleMeterRegistry.class,
        GithubHttpRequestFactory.class,
})
@AutoConfigureMockMvc
public class WebhookFilterIT {

    private static int webhookSignatureFailureCounter = 0;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    @Qualifier("webhookOrchestrator")
    private WebhookOrchestratorPort webhookOrchestratorPort;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubHttpRequestFactory githubHttpRequestFactory;

    @BeforeEach
    public void setup() {
        Mockito.when(
                webhookOrchestratorPort.process(Mockito.anyString(), Mockito.anyString(), Mockito.any(JsonNode.class)
                )).thenReturn(null);
    }

    @DisplayName("webhook.payload.successes increments")
    @Test
    public void givenValidPayload_WhenHttpRequestMade_ThenCustomMetricExists() {
        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestValid(jsonFileReader.getGithubPayloadValidAsString());

        try {
            mockMvc.perform(httpRequest).andReturn();
        } catch (Exception e) {
            fail("Unable to perform httpRequest", e);
        }

        Counter counter = meterRegistry
                .get("webhook.payload.successes")
                .tag("event", "push")
                .counter();

        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @DisplayName("No Signature: webhook.signature.failures increments")
    @Test
    public void givenPayloadNoSignature_WhenHttpRequestMade_ThenCustomMetricExists() {
        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestNoSignature(jsonFileReader.getGithubPayloadValidAsString());

        InvalidSignatureException invalidSignatureException = assertThrows(InvalidSignatureException.class, () -> mockMvc.perform(httpRequest));
        assertEquals("Missing X-Hub-Signature-256", invalidSignatureException.getMessage());
        webhookSignatureFailureCounter++;

        Counter counter = meterRegistry
                .get("webhook.signature.failures")
                .tag("event", "push")
                .counter();

        assertNotNull(counter);
        assertEquals(webhookSignatureFailureCounter, counter.count());
    }

    @DisplayName("Bad Signature: webhook.signature.failures increments")
    @Test
    public void givenPayloadInvalidSignature_WhenHttpRequestMade_ThenCustomMetricExists() {
        MockHttpServletRequestBuilder httpRequest = githubHttpRequestFactory.getHttpRequestInvalidSignature(jsonFileReader.getGithubPayloadValidAsString());

        InvalidSignatureException invalidSignatureException = assertThrows(InvalidSignatureException.class, () -> mockMvc.perform(httpRequest));
        assertEquals("Invalid X-Hub-Signature-256 - does not match rawBody", invalidSignatureException.getMessage());
        webhookSignatureFailureCounter++;

        Counter counter = meterRegistry
                .get("webhook.signature.failures")
                .tag("event", "push")
                .counter();

        assertNotNull(counter);
        assertEquals(webhookSignatureFailureCounter, counter.count());
    }

}


