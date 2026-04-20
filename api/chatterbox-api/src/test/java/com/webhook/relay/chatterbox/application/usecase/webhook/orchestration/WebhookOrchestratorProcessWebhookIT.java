package com.webhook.relay.chatterbox.application.usecase.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.webhook.relay.architecture_rules.quality.MirrorProductionClassForArchitectureRuleTests;
import com.webhook.relay.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import com.webhook.relay.chatterbox.adapter.in.validation.GithubWebhookValidator;
import com.webhook.relay.chatterbox.adapter.in.web.filter.WebhookFilter;
import com.webhook.relay.chatterbox.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import com.webhook.relay.chatterbox.adapter.out.persistence.WebhookPolledEventEventStoreJpaAdapter;
import com.webhook.relay.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import com.webhook.relay.chatterbox.adapter.out.webhook.poll.GithubRestPollingClient;
import com.webhook.relay.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import com.webhook.relay.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventReceived;
import com.webhook.relay.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;
import com.webhook.relay.chatterbox.common.config.InfrastructurePropertiesConfig;
import com.webhook.relay.chatterbox.common.logging.convenience.ImportSlf4jWebhookLogger;
import com.webhook.relay.chatterbox.common.logging.mdc.Slf4jMdcContext;
import com.webhook.relay.chatterbox.test.container.AbstractPostgresTestContainer;
import com.webhook.relay.chatterbox.test.helper.JsonFileReader;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ImportSlf4jWebhookLogger
@Import({
        WebhookOrchestrator.class,
        JsonFileReader.class,
        GithubWebhookValidator.class,
        GithubWebhookEventMapper.class,
        JacksonJsonConverter.class,
        InfrastructurePropertiesConfig.class,
        PropertiesConfigurationResolver.class,
        WebhookEventStoreJpaAdapter.class,
        Slf4jMdcContext.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@MirrorProductionClassForArchitectureRuleTests(WebhookOrchestrator.class)
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
        WebhookEventReceived webhookEvent = webhookOrchestratorPort.process(WebhookEventType.PUSH.name(), uniqueId, jsonNode.toString());
        assertNotNull(webhookEvent);
        assertNotNull(webhookEvent.id());
    }

}