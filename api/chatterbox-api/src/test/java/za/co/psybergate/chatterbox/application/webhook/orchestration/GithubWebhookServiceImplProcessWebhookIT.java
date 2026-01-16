package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import za.co.psybergate.chatterbox.application.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.application.persistence.dto.WebhookEventDto;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.github.delivery.GithubPollingServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.webhook.ingest.WebhookRequestValidatorImpl;
import za.co.psybergate.chatterbox.infrastructure.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.infrastructure.webhook.routing.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        GithubWebhookServiceImpl.class,
        JsonFileReader.class,
        WebhookRequestValidatorImpl.class,
        GithubEventExtractorImpl.class,
        JsonConverterImpl.class,
        ApplicationConfig.class,
        WebhookLoggerImpl.class,
        WebhookConfigurationResolverImpl.class,
        WebhookEventStoreJpaAdapter.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class GithubWebhookServiceImplProcessWebhookIT extends AbstractPostgresTestContainer {

    @MockitoBean
    private GithubPollingServiceImpl githubPollingService;

    @MockitoBean
    private WebhookRuntimeMetrics runtimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private GithubPolledEventStoreJpaAdapter githubPolledStore;

    @Autowired
    private GithubWebhookService githubWebhookService;

    @Autowired
    private JsonFileReader jsonFileReader;

    /// NOTE: The Event Publisher is not enabled by default with TestContainers
    /// This test should pass WITHOUT the Listener being executed
    @Test
    public void whenProcessWebhook_ThenEventPersisted() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        String uniqueId = UUID.randomUUID().toString();
        WebhookEventDto webhookEvent = githubWebhookService.process(EventType.PUSH.name(), uniqueId, jsonNode);
        assertNotNull(webhookEvent);
        assertNotNull(webhookEvent.id());
    }

}