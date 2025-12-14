package za.co.psybergate.chatterbox.application.infrastructure.web.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.chatterbox.application.webhook.orchestration.GithubWebhookService;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;
import za.co.psybergate.chatterbox.domain.utility.PayloadCryptor;
import za.co.psybergate.chatterbox.infrastructure.exception.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class WebhookFilterIT {

    // TODO BlakeGoudemond 2025/12/14 | extract these settings to WebhookConfig
    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    @Qualifier("githubWebhookServiceImpl")
    private GithubWebhookService githubWebhookService;

    @Autowired
    private PayloadCryptor payloadCryptor;

    @Autowired
    private JsonConverter jsonConverter;

    @BeforeEach
    public void setup() {
        Mockito.doNothing()
                .when(githubWebhookService).process(Mockito.anyString(), Mockito.any(JsonNode.class));
    }

    // TODO BlakeGoudemond 2025/12/14 | consider profile fr live tests with @EnabledIfEnvironmentVariable(named = "RUN_REAL_WEBHOOKS", matches = "true")
    // TODO BlakeGoudemond 2025/12/14 | extract to method like performWebhook - then put in helper class
    @DisplayName("webhook.payload.successes increments")
    @Test
    void givenValidPayload_WhenHttpRequestMade_ThenCustomMetricExists() {
        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret, readGithubPayload());

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
    void givenPayloadNoSignature_WhenHttpRequestMade_ThenCustomMetricExists() {
        MockHttpServletRequestBuilder httpRequest = getHttpRequestNoSignature(readGithubPayload());

        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class, () -> mockMvc.perform(httpRequest));
        assertEquals("Missing X-Hub-Signature-256", unauthorizedException.getMessage());

        Counter counter = meterRegistry
                .get("webhook.signature.failures")
                .tag("event", "push")
                .counter();

        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @DisplayName("Bad Signature: webhook.signature.failures increments")
    @Test
    void givenPayloadInvalidSignature_WhenHttpRequestMade_ThenCustomMetricExists() {
        MockHttpServletRequestBuilder httpRequest = getHttpRequestInvalidSignature(webhookSecret, readGithubPayload());

        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class, () -> mockMvc.perform(httpRequest));
        assertEquals("Invalid X-Hub-Signature-256 - does not match rawBody", unauthorizedException.getMessage());

        Counter counter = meterRegistry
                .get("webhook.signature.failures")
                .tag("event", "push")
                .counter();

        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    private MockHttpServletRequestBuilder getHttpRequestInvalidSignature(String payloadSecret, String payload) {
        String encryptedSignature = payloadCryptor.encryptUsingSHA256(payloadSecret, payload);
        encryptedSignature += "abc234";
        return post(apiPrefix + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push")
                .header("X-Hub-Signature-256", encryptedSignature);
    }

    private MockHttpServletRequestBuilder getHttpRequestNoSignature(String payload) {
        return post(apiPrefix + "/webhook/github")
                .contentType(APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(payload)
                .header("X-GitHub-Delivery", "123")
                .header("X-GitHub-Event", "push");
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


