package za.co.psybergate.chatterbox.adapter.out.teams.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.common.logging.ImportSlf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.logging.slf4j.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.adapter.out.http.model.HttpResponseDto;
import za.co.psybergate.chatterbox.application.domain.delivery.DeliveryResult;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.common.template.RegexTemplateSubstitutor;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.port.out.vendor.factory.VendorFactoryPort;
import za.co.psybergate.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import za.co.psybergate.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.adapter.out.teams.factory.TeamsAdaptiveCardFactory;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;
import za.co.psybergate.chatterbox.test.helper.TestConfigurationResolver;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ImportSlf4jWebhookLogger
@SpringBootTest(classes = {
        TeamsWebhookSender.class,
        JsonFileReader.class,
        JacksonJsonConverter.class,
        GithubWebhookEventMapper.class,
        PropertiesConfigurationResolver.class,
        TeamsAdaptiveCardFactory.class,
        RegexTemplateSubstitutor.class,
        InfrastructurePropertiesConfig.class,
        TestConfigurationResolver.class,
        PropertiesConfigurationResolver.class,
        Slf4jWebhookLogger.class,
        HttpResponseHandler.class,
})
@ActiveProfiles({"live-url"})
public class TeamsWebhookSenderIT {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private OutboundEventMapperPort eventExtractor;

    @Autowired
    private TeamsWebhookSender teamsWebhookSender;

    @Autowired
    @Qualifier("teamsAdaptiveCardFactory")
    private VendorFactoryPort teamsPayloadFactory;

    @Autowired
    private TestConfigurationResolver configurationResolver;

    /// Send an actual test to the MS Teams API and assert that the HttpResponse
    /// information is as-expected.
    ///
    /// This test is annotated with a Tag that `maven-surefire-plugin` is made aware of.
    /// This means that running `mvn clean install` will NOT include this by default
    @Tag("live-integration")
    @DisplayName("TeamsSenderService can process DTO")
    @Test
    public void givenGithubEventDto_WhenTeamsSenderServiceProcessesDto_ThenSuccess() {
        OutboundEvent outboundEvent = getOutboundEvent();
        String teamsDestinationUrl = configurationResolver.getTeamsDestinationUrl(outboundEvent);

        DeliveryResult deliveryResult = teamsWebhookSender.deliver(outboundEvent, teamsDestinationUrl);
        assertEquals(DeliveryResult.SUCCESS, deliveryResult);
    }

    @DisplayName("Bad HttpPost yields 401")
    @Test
    public void givenGithubEventDto_AndBadHttpPost_WhenExecuteHttp_ThenUnauthorised() {
        OutboundEvent outboundEvent = getOutboundEvent();
        String teamsDestinationUrl = configurationResolver.getTeamsDestinationUrl(outboundEvent);

        String jsonString = teamsPayloadFactory.getAsPayloadString(outboundEvent);
        HttpPost httpPost = getHttpPostWithAuthorizationHeaders(teamsDestinationUrl, jsonString);

        HttpResponseDto httpResponseDto = teamsWebhookSender.executeHttpPostRequest(httpPost);
        assertNotNull(httpResponseDto);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), httpResponseDto.httpStatus());
    }

    private HttpPost getHttpPostWithAuthorizationHeaders(String teamsDestination, String jsonString) {
        HttpPost httpPost = new HttpPost(teamsDestination);
        httpPost.setEntity(new StringEntity(jsonString, StandardCharsets.UTF_8));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer broken-test-token");
        return httpPost;
    }

    private OutboundEvent getOutboundEvent() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        return eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));
    }

}