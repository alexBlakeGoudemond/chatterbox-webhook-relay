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
import za.co.psybergate.chatterbox.application.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.application.persistence.dto.GithubPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.application.persistence.dto.GithubPolledEventDto;
import za.co.psybergate.chatterbox.application.persistence.dto.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.persistence.dto.WebhookEventDto;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.discord.delivery.DiscordSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.discord.factory.DiscordEmbeddedObjectFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.provider.ConfigurationProviderImpl;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.infrastructure.webhook.routing.WebhookConfigurationResolverImpl;
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
        GithubPolledEventStoreJpaAdapter.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        GithubEventExtractorImpl.class,
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
    private WebhookReceivedStore webhookReceivedStore;

    @Autowired
    private GithubPolledStore githubPolledStore;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventExtractor eventExtractor;

    private WebhookEventDto persistedWebhookEvent;

    private GithubPolledEventDto persistedGithubPolledEvent;

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
        eventProcessorService.processWebhookEvents();
        WebhookEventDto retrievedWebhookEvent = webhookReceivedStore.getWebhook(persistedWebhookEvent.id());
        assertNotNull(retrievedWebhookEvent);
        assertEquals(retrievedWebhookEvent.id(), persistedWebhookEvent.id());
        assertEquals(EventStatus.PROCESSED_SUCCESS, retrievedWebhookEvent.eventStatus());

        List<WebhookEventDeliveryDto> webhookEventDeliveryLogs = webhookReceivedStore.getDeliveryLogs(persistedWebhookEvent.id());
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
        GithubPolledEventDto retrievedPolledEvent = githubPolledStore.getEvent(persistedGithubPolledEvent.id());
        assertNotNull(retrievedPolledEvent);
        assertEquals(retrievedPolledEvent.id(), persistedGithubPolledEvent.id());
        assertEquals(EventStatus.PROCESSED_SUCCESS, retrievedPolledEvent.eventStatus());

        List<GithubPolledEventDeliveryDto> polledEventDeliveryLogs = githubPolledStore.getDeliveryLogs(persistedGithubPolledEvent.id());
        assertEquals(2, polledEventDeliveryLogs.size());
        for (GithubPolledEventDeliveryDto polledEventDeliveryLog : polledEventDeliveryLogs) {
            assertNotNull(polledEventDeliveryLog);
            assertEquals(polledEventDeliveryLog.githubPolledEventId(), retrievedPolledEvent.id());
        }
    }

}