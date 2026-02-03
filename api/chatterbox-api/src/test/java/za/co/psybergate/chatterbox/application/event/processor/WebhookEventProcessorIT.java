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
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.delivery.model.CompositeEventDeliveryAdapter;
import za.co.psybergate.chatterbox.adapter.out.discord.delivery.DiscordWebhookSender;
import za.co.psybergate.chatterbox.adapter.out.discord.factory.DiscordEmbeddedObjectFactory;
import za.co.psybergate.chatterbox.adapter.out.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.adapter.out.persistence.WebhookPolledEventEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.adapter.out.teams.delivery.TeamsWebhookSender;
import za.co.psybergate.chatterbox.adapter.out.teams.factory.TeamsAdaptiveCardFactory;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.application.common.logging.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.template.RegexTemplateSubstitutor;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import za.co.psybergate.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.*;
import za.co.psybergate.chatterbox.application.port.in.event.processor.EventProcessorPort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.usecase.event.processor.WebhookEventProcessor;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        WebhookEventProcessor.class,
        WebhookEventStoreJpaAdapter.class,
        WebhookPolledEventEventStoreJpaAdapter.class,
        JsonFileReader.class,
        JacksonJsonConverter.class,
        GithubWebhookEventMapper.class,
        Slf4jWebhookLogger.class,
        TeamsWebhookSender.class,
        TeamsAdaptiveCardFactory.class,
        DiscordWebhookSender.class,
        DiscordEmbeddedObjectFactory.class,
        RegexTemplateSubstitutor.class,
        InfrastructurePropertiesConfig.class,
        PropertiesConfigurationResolver.class,
        HttpResponseHandler.class,
        CompositeEventDeliveryAdapter.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles({"test", "live-url"})
public class WebhookEventProcessorIT extends AbstractPostgresTestContainer {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private EventProcessorPort eventProcessor;

    @Autowired
    private WebhookEventStorePort webhookEventStorePort;

    @Autowired
    private WebhookPolledEventStorePort webhookPolledEventStorePort;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private OutboundEventMapperPort eventExtractor;

    private WebhookEventReceivedDto persistedWebhookEvent;

    private WebhookPolledEventReceivedDto persistedGithubPolledEvent;

    @BeforeEach
    public void setup() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, jsonNode);
        String uniqueId = UUID.randomUUID().toString();
        this.persistedWebhookEvent = webhookEventStorePort.storeWebhook(uniqueId, outboundEvent, jsonNode);
        this.persistedGithubPolledEvent = webhookPolledEventStorePort.storeEvent(uniqueId, outboundEvent, jsonNode);
    }

    @DisplayName("Processing Webhook Events creates Delivery Logs")
    @Tag("live-integration")
    @Test
    public void whenProcessWebhookEvents_ThenEventStatusUpdated_AndDeliveryLogExists() {
        eventProcessor.processWebhookEvents();
        WebhookEventReceivedDto retrievedWebhookEvent = webhookEventStorePort.getWebhook(persistedWebhookEvent.id());
        assertNotNull(retrievedWebhookEvent);
        assertEquals(retrievedWebhookEvent.id(), persistedWebhookEvent.id());
        assertEquals(WebhookEventStatus.PROCESSED_SUCCESS, retrievedWebhookEvent.webhookEventStatus());

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
        WebhookPolledEventReceivedDto retrievedPolledEvent = webhookPolledEventStorePort.getEvent(persistedGithubPolledEvent.id());
        assertNotNull(retrievedPolledEvent);
        assertEquals(retrievedPolledEvent.id(), persistedGithubPolledEvent.id());
        assertEquals(WebhookEventStatus.PROCESSED_SUCCESS, retrievedPolledEvent.webhookEventStatus());

        List<WebhookPolledEventDeliveryDto> polledEventDeliveryLogs = webhookPolledEventStorePort.getDeliveryLogs(persistedGithubPolledEvent.id());
        assertEquals(2, polledEventDeliveryLogs.size());
        for (WebhookPolledEventDeliveryDto polledEventDeliveryLog : polledEventDeliveryLogs) {
            assertNotNull(polledEventDeliveryLog);
            assertEquals(polledEventDeliveryLog.githubPolledEventId(), retrievedPolledEvent.id());
        }
    }


}