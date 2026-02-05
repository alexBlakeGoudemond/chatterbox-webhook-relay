package za.co.psybergate.chatterbox.application.usecase.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import za.co.psybergate.architecture_rules.quality.MirrorProductionClassForArchitectureRuleTests;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;
import za.co.psybergate.chatterbox.application.common.logging.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.usecase.webhook.orchestration.WebhookOrchestrator;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventReceived;
import za.co.psybergate.chatterbox.adapter.in.validation.GithubWebhookValidator;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.webhook.poll.GithubRestPollingClient;
import za.co.psybergate.chatterbox.adapter.out.persistence.WebhookPolledEventEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        WebhookOrchestrator.class,
        JsonFileReader.class,
        GithubWebhookValidator.class,
        GithubWebhookEventMapper.class,
        JacksonJsonConverter.class,
        InfrastructurePropertiesConfig.class,
        Slf4jWebhookLogger.class,
        PropertiesConfigurationResolver.class,
        WebhookEventStoreJpaAdapter.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@MirrorProductionClassForArchitectureRuleTests("WebhookOrchestrator")
public class WebhookOrchestratorProcessWebhookIT extends AbstractPostgresTestContainer {

    @MockitoBean
    private GithubRestPollingClient githubPollingService;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics runtimeMetrics;

    @MockitoBean
    private WebhookPolledEventEventStoreJpaAdapter githubPolledStore;

    @Autowired
    private WebhookOrchestratorPort webhookOrchestratorPort;

    @Autowired
    private JsonFileReader jsonFileReader;

    /// NOTE: The Event Publisher is not enabled by default with TestContainers
    /// This test should pass WITHOUT the Listener being executed
    @Test
    public void whenProcessWebhook_ThenEventPersisted() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        String uniqueId = UUID.randomUUID().toString();
        WebhookEventReceived webhookEvent = webhookOrchestratorPort.process(WebhookEventType.PUSH.name(), uniqueId, jsonNode);
        assertNotNull(webhookEvent);
        assertNotNull(webhookEvent.id());
    }

}