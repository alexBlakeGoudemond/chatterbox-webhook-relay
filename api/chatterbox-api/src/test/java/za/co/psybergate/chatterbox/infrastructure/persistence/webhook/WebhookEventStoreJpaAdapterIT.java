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
import za.co.psybergate.chatterbox.application.common.logging.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.WebhookEvent;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
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

        WebhookEventReceivedDto webhookEvent = adapter.storeWebhook("abc123", eventDto, jsonNode);
        assertNotNull(webhookEvent);
    }

    @DisplayName("Can save WebhookEventLog")
    @Test
    public void givenWebhookEvent_WhenStoreDelivery_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.map(WebhookEventType.PUSH, jsonNode);
        WebhookEvent webhookEvent = new WebhookEvent("abc123", eventDto, jsonNode);
        WebhookEventReceivedDto webhookEventReceivedDto = WebhookEventStoreJpaAdapter.mapToWebhookEventRecord(webhookEvent);
        WebhookEventDeliveryDto webhookEventDeliveryLog = adapter.storeSuccessfulDelivery(webhookEventReceivedDto, "exampleDestination", "exampleDestinationUrl");
        assertNotNull(webhookEventDeliveryLog);
    }

}