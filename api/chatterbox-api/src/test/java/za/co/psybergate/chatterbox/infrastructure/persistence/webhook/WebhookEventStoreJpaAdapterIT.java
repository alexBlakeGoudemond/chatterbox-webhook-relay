package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;
import za.co.psybergate.chatterbox.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.WebhookEvent;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.application.common.logging.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        WebhookEventStoreJpaAdapter.class,
        JsonFileReader.class,
        JacksonJsonConverter.class,
        GithubWebhookEventMapper.class,
        PropertiesConfigurationResolver.class,
        InfrastructurePropertiesConfig.class,
        Slf4jWebhookLogger.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class WebhookEventStoreJpaAdapterIT extends AbstractPostgresTestContainer {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private WebhookEventStoreJpaAdapter adapter;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventMapper eventExtractor;

    @DisplayName("Can save WebhookEvent")
    @Test
    public void givenPayloadAndEventDto_WhenStoreWebhook_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.map(WebhookEventType.PUSH, jsonNode);
        OutboundEvent outboundEvent = mapToOutboundEvent(WebhookEventStoreJpaAdapter.mapToWebhookEventReceivedDto(mapToWebhookEvent(eventDto, jsonNode)), jsonNode);

        WebhookEventReceivedDto webhookEvent = adapter.storeWebhook("abc123", outboundEvent, jsonNode);
        assertNotNull(webhookEvent);
    }

    @DisplayName("Can save WebhookEventLog")
    @Test
    public void givenWebhookEvent_WhenStoreDelivery_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.map(WebhookEventType.PUSH, jsonNode);
        WebhookEvent webhookEvent = mapToWebhookEvent(eventDto, jsonNode);
        WebhookEventReceivedDto webhookEventReceivedDto = WebhookEventStoreJpaAdapter.mapToWebhookEventReceivedDto(webhookEvent);
        OutboundEvent outboundEvent = mapToOutboundEvent(webhookEventReceivedDto, jsonNode);
        WebhookEventDeliveryDto webhookEventDeliveryLog = adapter.storeSuccessfulDelivery(outboundEvent, "exampleDestination", "exampleDestinationUrl");
        assertNotNull(webhookEventDeliveryLog);
    }

    private WebhookEvent mapToWebhookEvent(GithubEventDto eventDto, JsonNode jsonNode) {
        return new WebhookEvent("abc123",
                eventDto.repositoryName(),
                eventDto.webhookEventType(),
                eventDto.displayName(),
                eventDto.senderName(),
                eventDto.urlDisplayText(),
                eventDto.urlDisplayText(),
                eventDto.extraDetail(),
                jsonNode.toString());
    }

    public static OutboundEvent mapToOutboundEvent(WebhookEventReceivedDto webhookEventReceivedDto, JsonNode jsonNode) {
        return new OutboundEvent(
                1L,
                "0123456789abcde",
                WebhookEventType.PUSH.name(),
                webhookEventReceivedDto.displayName(),
                webhookEventReceivedDto.repositoryFullName(),
                webhookEventReceivedDto.senderName(),
                webhookEventReceivedDto.eventUrl(),
                webhookEventReceivedDto.eventUrlDisplayText(),
                webhookEventReceivedDto.extraDetail(),
                jsonNode.toString()
        );
    }

}