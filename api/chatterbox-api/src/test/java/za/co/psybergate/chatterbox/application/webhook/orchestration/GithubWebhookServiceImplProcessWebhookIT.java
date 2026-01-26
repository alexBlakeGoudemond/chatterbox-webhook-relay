package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.GithubWebhookPort;
import za.co.psybergate.chatterbox.application.common.logging.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.usecase.webhook.orchestration.GithubWebhookOrchestrator;
import za.co.psybergate.chatterbox.application.domain.api.EventType;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventDto;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.validation.GithubWebhookValidator;
import za.co.psybergate.chatterbox.infrastructure.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.github.delivery.GithubRestPollingClient;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.GithubPolledEventEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        GithubWebhookOrchestrator.class,
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
public class GithubWebhookServiceImplProcessWebhookIT extends AbstractPostgresTestContainer {

    @MockitoBean
    private GithubRestPollingClient githubPollingService;

    @MockitoBean
    private WebhookRuntimeMetrics runtimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private GithubPolledEventEventStoreJpaAdapter githubPolledStore;

    @Autowired
    private GithubWebhookPort githubWebhookPort;

    @Autowired
    private JsonFileReader jsonFileReader;

    /// NOTE: The Event Publisher is not enabled by default with TestContainers
    /// This test should pass WITHOUT the Listener being executed
    @Test
    public void whenProcessWebhook_ThenEventPersisted() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        String uniqueId = UUID.randomUUID().toString();
        WebhookEventDto webhookEvent = githubWebhookPort.process(EventType.PUSH.name(), uniqueId, jsonNode);
        assertNotNull(webhookEvent);
        assertNotNull(webhookEvent.id());
    }

}