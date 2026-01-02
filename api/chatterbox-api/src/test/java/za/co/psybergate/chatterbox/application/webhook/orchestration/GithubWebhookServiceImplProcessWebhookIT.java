package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import za.co.psybergate.chatterbox.application.github.delivery.GithubPollingService;
import za.co.psybergate.chatterbox.application.github.delivery.GithubPollingServiceImpl;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.webhook.ingest.WebhookRequestValidatorImpl;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.application.webhook.security.PayloadCryptorImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.helper.JsonFileReader;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

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
        WebhookLogger.class,
        WebhookConfigurationResolverImpl.class,
        WebhookEventStoreJpaAdapter.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Testcontainers
public class GithubWebhookServiceImplProcessWebhookIT {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("chatterbox")
                    .withUsername("user")
                    .withPassword("password");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

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

    @Test
    @Rollback
    public void whenProcessWebhook_ThenEventPersisted() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        String uniqueId = UUID.randomUUID().toString();
        WebhookEvent webhookEvent = githubWebhookService.process(EventType.PUSH.name(), uniqueId, jsonNode);
        assertNotNull(webhookEvent);
        assertNotNull(webhookEvent.getId());
    }

}