package za.co.psybergate.chatterbox.application.event.processor;

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
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledEventStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookEventStore;
import za.co.psybergate.chatterbox.application.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.application.persistence.dto.GithubPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.application.persistence.dto.GithubPolledEventDto;
import za.co.psybergate.chatterbox.application.persistence.dto.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.persistence.dto.WebhookEventDto;
import za.co.psybergate.chatterbox.infrastructure.event.processor.EventProcessorServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.web.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.discord.delivery.DiscordSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.discord.factory.DiscordEmbeddedObjectFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.config.provider.ConfigurationProviderImpl;
import za.co.psybergate.chatterbox.infrastructure.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.application.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.webhook.mapper.GithubEventMapperImpl;
import za.co.psybergate.chatterbox.infrastructure.webhook.resolution.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        EventProcessorServiceImpl.class,
        WebhookEventStoreJpaAdapter.class,
        GithubPolledEventEventStoreJpaAdapter.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        GithubEventMapperImpl.class,
        WebhookLoggerImpl.class,
        TeamsSenderServiceImpl.class,
        TeamsCardFactoryImpl.class,
        DiscordSenderServiceImpl.class,
        DiscordEmbeddedObjectFactoryImpl.class,
        TemplateSubstitutorImpl.class,
        ApplicationConfig.class,
        WebhookConfigurationResolverImpl.class,
        HttpResponseHandler.class,
        ConfigurationProviderImpl.class
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
    private EventProcessorService eventProcessorService;

    @Autowired
    private WebhookEventStore webhookEventStore;

    @Autowired
    private GithubPolledEventStore githubPolledEventStore;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventMapper eventExtractor;

    private WebhookEventDto persistedWebhookEvent;

    private GithubPolledEventDto persistedGithubPolledEvent;

    @BeforeEach
    public void setup() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.map(EventType.PUSH, jsonNode);
        String uniqueId = UUID.randomUUID().toString();
        this.persistedWebhookEvent = webhookEventStore.storeWebhook(uniqueId, eventDto, jsonNode);
        this.persistedGithubPolledEvent = githubPolledEventStore.storeEvent(uniqueId, eventDto, jsonNode);
    }

    @DisplayName("Processing Webhook Events creates Delivery Logs")
    @Tag("live-integration")
    @Test
    public void whenProcessWebhookEvents_ThenEventStatusUpdated_AndDeliveryLogExists() {
        eventProcessorService.processWebhookEvents();
        WebhookEventDto retrievedWebhookEvent = webhookEventStore.getWebhook(persistedWebhookEvent.id());
        assertNotNull(retrievedWebhookEvent);
        assertEquals(retrievedWebhookEvent.id(), persistedWebhookEvent.id());
        assertEquals(EventStatus.PROCESSED_SUCCESS, retrievedWebhookEvent.eventStatus());

        List<WebhookEventDeliveryDto> webhookEventDeliveryLogs = webhookEventStore.getDeliveryLogs(persistedWebhookEvent.id());
        assertEquals(2, webhookEventDeliveryLogs.size());
        for (WebhookEventDeliveryDto webhookEventDeliveryLog : webhookEventDeliveryLogs) {
            assertNotNull(webhookEventDeliveryLog);
            assertEquals(webhookEventDeliveryLog.webhookEventId(), retrievedWebhookEvent.id());
        }
    }

    @DisplayName("Processing Github Polled Events creates Delivery Logs")
    @Tag("live-integration")
    @Test
    public void whenProcessGithubPolledEvents_ThenEventStatusUpdated_AndDeliveryLogExists() {
        eventProcessorService.processPolledEvents();
        GithubPolledEventDto retrievedPolledEvent = githubPolledEventStore.getEvent(persistedGithubPolledEvent.id());
        assertNotNull(retrievedPolledEvent);
        assertEquals(retrievedPolledEvent.id(), persistedGithubPolledEvent.id());
        assertEquals(EventStatus.PROCESSED_SUCCESS, retrievedPolledEvent.eventStatus());

        List<GithubPolledEventDeliveryDto> polledEventDeliveryLogs = githubPolledEventStore.getDeliveryLogs(persistedGithubPolledEvent.id());
        assertEquals(2, polledEventDeliveryLogs.size());
        for (GithubPolledEventDeliveryDto polledEventDeliveryLog : polledEventDeliveryLogs) {
            assertNotNull(polledEventDeliveryLog);
            assertEquals(polledEventDeliveryLog.githubPolledEventId(), retrievedPolledEvent.id());
        }
    }

}