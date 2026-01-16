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
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.infrastructure.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.event.WebhookEventDeliveryRecord;
import za.co.psybergate.chatterbox.domain.event.WebhookEventRecord;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        WebhookEventStoreJpaAdapter.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        GithubEventExtractorImpl.class,
        WebhookConfigurationResolverImpl.class,
        ApplicationConfig.class,
        WebhookLoggerImpl.class,
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
    private GithubEventExtractor eventExtractor;

    @DisplayName("Can save WebhookEvent")
    @Test
    public void givenPayloadAndEventDto_WhenStoreWebhook_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.extract(EventType.PUSH, jsonNode);

        WebhookEventRecord webhookEvent = adapter.storeWebhook("abc123", eventDto, jsonNode);
        assertNotNull(webhookEvent);
    }

    @DisplayName("Can save WebhookEventLog")
    @Test
    public void givenWebhookEvent_WhenStoreDelivery_ThenSuccess() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.extract(EventType.PUSH, jsonNode);
        WebhookEvent webhookEvent = new WebhookEvent("abc123", eventDto, jsonNode);
        WebhookEventRecord webhookEventRecord = WebhookEventStoreJpaAdapter.mapToWebhookEventRecord(webhookEvent);
        webhookEventRecord.setId(1L);
        WebhookEventDeliveryRecord webhookEventDeliveryLog = adapter.storeSuccessfulDelivery(webhookEventRecord, "exampleDestination", "exampleDestinationUrl");
        assertNotNull(webhookEventDeliveryLog);
    }

}