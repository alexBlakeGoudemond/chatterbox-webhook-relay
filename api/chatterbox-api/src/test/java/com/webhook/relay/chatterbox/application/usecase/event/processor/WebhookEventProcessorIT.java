package com.webhook.relay.chatterbox.application.usecase.event.processor;

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
import com.webhook.relay.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import com.webhook.relay.chatterbox.adapter.in.web.filter.WebhookFilter;
import com.webhook.relay.chatterbox.adapter.out.delivery.model.CompositeEventDeliveryAdapter;
import com.webhook.relay.chatterbox.adapter.out.discord.delivery.DiscordWebhookSender;
import com.webhook.relay.chatterbox.adapter.out.discord.factory.DiscordEmbeddedObjectFactory;
import com.webhook.relay.chatterbox.adapter.out.http.HttpResponseHandler;
import com.webhook.relay.chatterbox.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import com.webhook.relay.chatterbox.adapter.out.persistence.WebhookPolledEventEventStoreJpaAdapter;
import com.webhook.relay.chatterbox.adapter.out.teams.delivery.TeamsWebhookSender;
import com.webhook.relay.chatterbox.adapter.out.teams.factory.TeamsAdaptiveCardFactory;
import com.webhook.relay.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import com.webhook.relay.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import com.webhook.relay.chatterbox.application.common.template.RegexTemplateSubstitutor;
import com.webhook.relay.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventStatus;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventDelivery;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventReceived;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventDelivery;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventReceived;
import com.webhook.relay.chatterbox.application.port.in.event.processor.EventProcessorPort;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import com.webhook.relay.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import com.webhook.relay.chatterbox.common.config.InfrastructurePropertiesConfig;
import com.webhook.relay.chatterbox.common.logging.convenience.ImportSlf4jWebhookLogger;
import com.webhook.relay.chatterbox.test.container.AbstractPostgresTestContainer;
import com.webhook.relay.chatterbox.test.helper.JsonFileReader;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ImportSlf4jWebhookLogger
@Import({
        WebhookEventProcessor.class,
        WebhookEventStoreJpaAdapter.class,
        WebhookPolledEventEventStoreJpaAdapter.class,
        JsonFileReader.class,
        JacksonJsonConverter.class,
        GithubWebhookEventMapper.class,
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

    private WebhookEventReceived persistedWebhookEvent;

    private WebhookPolledEventReceived persistedGithubPolledEvent;

    @BeforeEach
    public void setup() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));
        String uniqueId = UUID.randomUUID().toString();
        this.persistedWebhookEvent = webhookEventStorePort.storeWebhook(uniqueId, outboundEvent, RawEventPayload.of(jsonNode));
        this.persistedGithubPolledEvent = webhookPolledEventStorePort.storeEvent(uniqueId, outboundEvent, RawEventPayload.of(jsonNode));
    }

    @DisplayName("Processing Webhook Events creates Delivery Logs")
    @Tag("live-integration")
    @Test
    public void whenProcessWebhookEvents_ThenEventStatusUpdated_AndDeliveryLogExists() {
        eventProcessor.processWebhookEvent("alexBlakeGoudemond/chatterbox");
        WebhookEventReceived retrievedWebhookEvent = webhookEventStorePort.getWebhook(persistedWebhookEvent.id());
        assertNotNull(retrievedWebhookEvent);
        assertEquals(retrievedWebhookEvent.id(), persistedWebhookEvent.id());
        assertEquals(WebhookEventStatus.PROCESSED_SUCCESS, retrievedWebhookEvent.webhookEventStatus());

        List<WebhookEventDelivery> webhookEventDeliveryLogs = webhookEventStorePort.getDeliveryLogs(persistedWebhookEvent.id());
        assertEquals(2, webhookEventDeliveryLogs.size());
        for (WebhookEventDelivery webhookEventDeliveryLog : webhookEventDeliveryLogs) {
            assertNotNull(webhookEventDeliveryLog);
            assertEquals(webhookEventDeliveryLog.webhookEventId(), retrievedWebhookEvent.id());
        }
    }

    @DisplayName("Processing Github Polled Events creates Delivery Logs")
    @Tag("live-integration")
    @Test
    public void whenProcessGithubPolledEvents_ThenEventStatusUpdated_AndDeliveryLogExists() {
        eventProcessor.processPolledEvents();
        WebhookPolledEventReceived retrievedPolledEvent = webhookPolledEventStorePort.getEvent(persistedGithubPolledEvent.id());
        assertNotNull(retrievedPolledEvent);
        assertEquals(retrievedPolledEvent.id(), persistedGithubPolledEvent.id());
        assertEquals(WebhookEventStatus.PROCESSED_SUCCESS, retrievedPolledEvent.webhookEventStatus());

        List<WebhookPolledEventDelivery> polledEventDeliveryLogs = webhookPolledEventStorePort.getDeliveryLogs(persistedGithubPolledEvent.id());
        assertEquals(2, polledEventDeliveryLogs.size());
        for (WebhookPolledEventDelivery polledEventDeliveryLog : polledEventDeliveryLogs) {
            assertNotNull(polledEventDeliveryLog);
            assertEquals(polledEventDeliveryLog.githubPolledEventId(), retrievedPolledEvent.id());
        }
    }


}