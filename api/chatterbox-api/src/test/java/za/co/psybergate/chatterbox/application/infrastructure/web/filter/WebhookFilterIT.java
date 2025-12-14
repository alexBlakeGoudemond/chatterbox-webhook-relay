package za.co.psybergate.chatterbox.application.infrastructure.web.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import za.co.psybergate.chatterbox.domain.utility.JsonConverter;
import za.co.psybergate.chatterbox.domain.utility.PayloadCryptor;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WebhookFilterIT {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${webhook.github.secret}")
    private String webhookSecret;

    @Autowired
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PayloadCryptor payloadCryptor;

    @Autowired
    private JsonConverter jsonConverter;

    // TODO BlakeGoudemond 2025/12/14 | debug why not working
    @Test
    void filterMetricsAreRecorded() throws Exception {
        MockHttpServletRequestBuilder httpRequest = getHttpRequestValid(webhookSecret, readGithubPayload());
        mockMvc.perform(httpRequest)
                .andReturn(); // status does not matter

        Counter counter = meterRegistry
                .get("webhook.payload.successes")
                .tag("event", "push")
                .counter();

        Assertions.assertNotNull(counter);
        Assertions.assertEquals(1.0, counter.count());

        Timer timer = meterRegistry
                .find("http.requests.filter.duration")
                .timer();

        Assertions.assertNotNull(timer);
        Assertions.assertEquals(1, timer.count());
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


