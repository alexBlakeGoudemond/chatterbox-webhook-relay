package za.co.psybergate.chatterbox.application.processor;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.application.teams.factory.template.TeamsTemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventDeliveryLog;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventDeliveryLog;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        EventProcessorImpl.class,
        WebhookEventStoreJpaAdapter.class,
        GithubPolledEventStoreJpaAdapter.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        GithubEventExtractorImpl.class,
        WebhookLogger.class,
        TeamsSenderServiceImpl.class,
        TeamsCardFactoryImpl.class,
        TeamsTemplateSubstitutorImpl.class,
        ApplicationConfig.class,
        WebhookConfigurationResolverImpl.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles({"test", "live-url"})
public class EventProcessorImplIT extends AbstractPostgresTestContainer {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private EventProcessor eventProcessor;

    @Autowired
    private WebhookReceivedStore webhookReceivedStore;

    @Autowired
    private GithubPolledStore githubPolledStore;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventExtractor eventExtractor;

    private WebhookEvent persistedWebhookEvent;

    private GithubPolledEvent persistedGithubPolledEvent;

    @BeforeEach
    public void setup() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.extract(EventType.PUSH, jsonNode);
        String uniqueId = UUID.randomUUID().toString();
        this.persistedWebhookEvent = webhookReceivedStore.storeWebhook(uniqueId, eventDto, jsonNode);
        this.persistedGithubPolledEvent = githubPolledStore.storeEvent(uniqueId, eventDto, jsonNode);
    }

    @DisplayName("Processing Webhook Events creates Delivery Logs")
    @Tag("live-integration")
    @Test
    public void whenProcessWebhookEvents_ThenEventStatusUpdated_AndDeliveryLogExists() {
        eventProcessor.processWebhookEvents();
        WebhookEvent retrievedWebhookEvent = webhookReceivedStore.getWebhook(persistedWebhookEvent.getId());
        assertNotNull(retrievedWebhookEvent);
        assertEquals(retrievedWebhookEvent.getId(), persistedWebhookEvent.getId());
        assertEquals(EventStatus.PROCESSED_SUCCESS, retrievedWebhookEvent.getEventStatus());

        List<WebhookEventDeliveryLog> webhookEventDeliveryLogs = webhookReceivedStore.getDeliveryLogs(persistedWebhookEvent.getId());
        for (WebhookEventDeliveryLog webhookEventDeliveryLog : webhookEventDeliveryLogs) {
            assertNotNull(webhookEventDeliveryLog);
            assertEquals(webhookEventDeliveryLog.getWebhookEventId(), retrievedWebhookEvent.getId());
        }
    }

    @DisplayName("Processing Github Polled Events creates Delivery Logs")
    @Tag("live-integration")
    @Test
    public void whenProcessGithubPolledEvents_ThenEventStatusUpdated_AndDeliveryLogExists(){
        eventProcessor.processPolledEvents();
        GithubPolledEvent retrievedPolledEvent = githubPolledStore.getEvent(persistedGithubPolledEvent.getId());
        assertNotNull(retrievedPolledEvent);
        assertEquals(retrievedPolledEvent.getId(), persistedGithubPolledEvent.getId());
        assertEquals(EventStatus.PROCESSED_SUCCESS, retrievedPolledEvent.getEventStatus());

        List<GithubPolledEventDeliveryLog> polledEventDeliveryLogs = githubPolledStore.getDeliveryLogs(persistedGithubPolledEvent.getId());
        for (GithubPolledEventDeliveryLog polledEventDeliveryLog : polledEventDeliveryLogs) {
            assertNotNull(polledEventDeliveryLog);
            assertEquals(polledEventDeliveryLog.getGithubPolledEventId(), retrievedPolledEvent.getId());
        }
    }

}