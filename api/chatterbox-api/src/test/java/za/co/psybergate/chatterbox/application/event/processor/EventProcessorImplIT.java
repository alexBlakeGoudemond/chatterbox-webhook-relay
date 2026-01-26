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
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.common.event.processor.EventProcessor;
import za.co.psybergate.chatterbox.application.common.event.processor.EventProcessorImpl;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.application.common.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubEventMapperImpl;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.event.model.*;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.discord.factory.DiscordEmbeddedObjectFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.teams.factory.TeamsCardFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.discord.delivery.DiscordSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.GithubPolledEventEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.webhook.resolution.WebhookConfigurationResolverImpl;
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
        InfrastructurePropertiesConfig.class,
        WebhookConfigurationResolverImpl.class,
        HttpResponseHandler.class,
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
    private WebhookEventStorePort webhookEventStorePort;

    @Autowired
    private GithubPolledEventStorePort githubPolledEventStorePort;

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
        this.persistedWebhookEvent = webhookEventStorePort.storeWebhook(uniqueId, eventDto, jsonNode);
        this.persistedGithubPolledEvent = githubPolledEventStorePort.storeEvent(uniqueId, eventDto, jsonNode);
    }

    @DisplayName("Processing Webhook Events creates Delivery Logs")
    @Tag("live-integration")
    @Test
    public void whenProcessWebhookEvents_ThenEventStatusUpdated_AndDeliveryLogExists() {
        eventProcessor.processWebhookEvents();
        WebhookEventDto retrievedWebhookEvent = webhookEventStorePort.getWebhook(persistedWebhookEvent.id());
        assertNotNull(retrievedWebhookEvent);
        assertEquals(retrievedWebhookEvent.id(), persistedWebhookEvent.id());
        assertEquals(EventStatus.PROCESSED_SUCCESS, retrievedWebhookEvent.eventStatus());

        List<WebhookEventDeliveryDto> webhookEventDeliveryLogs = webhookEventStorePort.getDeliveryLogs(persistedWebhookEvent.id());
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
        eventProcessor.processPolledEvents();
        GithubPolledEventDto retrievedPolledEvent = githubPolledEventStorePort.getEvent(persistedGithubPolledEvent.id());
        assertNotNull(retrievedPolledEvent);
        assertEquals(retrievedPolledEvent.id(), persistedGithubPolledEvent.id());
        assertEquals(EventStatus.PROCESSED_SUCCESS, retrievedPolledEvent.eventStatus());

        List<GithubPolledEventDeliveryDto> polledEventDeliveryLogs = githubPolledEventStorePort.getDeliveryLogs(persistedGithubPolledEvent.id());
        assertEquals(2, polledEventDeliveryLogs.size());
        for (GithubPolledEventDeliveryDto polledEventDeliveryLog : polledEventDeliveryLogs) {
            assertNotNull(polledEventDeliveryLog);
            assertEquals(polledEventDeliveryLog.githubPolledEventId(), retrievedPolledEvent.id());
        }
    }

}