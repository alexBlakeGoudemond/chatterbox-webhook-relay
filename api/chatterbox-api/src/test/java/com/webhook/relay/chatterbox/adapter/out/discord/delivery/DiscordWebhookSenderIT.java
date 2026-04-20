package com.webhook.relay.chatterbox.adapter.out.discord.delivery;

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
import com.webhook.relay.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import com.webhook.relay.chatterbox.adapter.in.web.filter.WebhookFilter;
import com.webhook.relay.chatterbox.adapter.out.discord.factory.DiscordEmbeddedObjectFactory;
import com.webhook.relay.chatterbox.adapter.out.http.HttpResponseHandler;
import com.webhook.relay.chatterbox.adapter.out.http.model.HttpResponseDto;
import com.webhook.relay.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import com.webhook.relay.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import com.webhook.relay.chatterbox.application.common.logging.slf4j.Slf4jWebhookLogger;
import com.webhook.relay.chatterbox.application.common.template.RegexTemplateSubstitutor;
import com.webhook.relay.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryResult;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.port.out.vendor.factory.VendorFactoryPort;
import com.webhook.relay.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import com.webhook.relay.chatterbox.common.config.InfrastructurePropertiesConfig;
import com.webhook.relay.chatterbox.common.logging.convenience.ImportSlf4jWebhookLogger;
import com.webhook.relay.chatterbox.test.helper.JsonFileReader;
import com.webhook.relay.chatterbox.test.helper.TestConfigurationResolver;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ImportSlf4jWebhookLogger
@SpringBootTest(classes = {
        JsonFileReader.class,
        JacksonJsonConverter.class,
        GithubWebhookEventMapper.class,
        PropertiesConfigurationResolver.class,
        DiscordWebhookSender.class,
        DiscordEmbeddedObjectFactory.class,
        RegexTemplateSubstitutor.class,
        InfrastructurePropertiesConfig.class,
        TestConfigurationResolver.class,
        PropertiesConfigurationResolver.class,
        Slf4jWebhookLogger.class,
        HttpResponseHandler.class,
})
@ActiveProfiles({"live-url"})
public class DiscordWebhookSenderIT {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private OutboundEventMapperPort eventExtractor;

    @Autowired
    private DiscordWebhookSender discordWebhookSender;

    @Autowired
    @Qualifier("discordEmbeddedObjectFactory")
    private VendorFactoryPort discordPayloadFactory;

    @Autowired
    private TestConfigurationResolver configurationResolver;

    /// Send an actual test to the Discord API and assert that the HttpResponse
    /// information is as-expected.
    ///
    /// This test is annotated with a Tag that `maven-surefire-plugin` is made aware of.
    /// This means that running `mvn clean install` will NOT include this by default
    @Tag("live-integration")
    @DisplayName("DiscordSenderService can process DTO")
    @Test
    public void givenGithubEventDto_WhenDiscordSenderServiceProcessesDto_ThenSuccess() {
        OutboundEvent outboundEvent = getGithubEventDto();
        String teamsDestinationUrl = configurationResolver.getDiscordDestinationUrl(outboundEvent);

        DeliveryResult httpResponseDto = discordWebhookSender.deliver(outboundEvent, teamsDestinationUrl);
        assertEquals(DeliveryResult.SUCCESS, httpResponseDto);
    }

    @DisplayName("Bad HttpPost yields 401")
    @Test
    public void givenGithubEventDto_AndBadHttpPost_WhenExecuteHttp_ThenUnauthorised() {
        OutboundEvent outboundEvent = getGithubEventDto();
        String teamsDestinationUrl = configurationResolver.getTeamsDestinationUrl(outboundEvent);

        String jsonString = discordPayloadFactory.getAsPayloadString(outboundEvent);
        HttpPost httpPost = getHttpPostWithAuthorizationHeaders(teamsDestinationUrl, jsonString);

        HttpResponseDto httpResponseDto = discordWebhookSender.executeHttpPostRequest(httpPost);
        assertNotNull(httpResponseDto);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), httpResponseDto.httpStatus());
    }

    private OutboundEvent getGithubEventDto() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        return eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));
    }

    private HttpPost getHttpPostWithAuthorizationHeaders(String teamsDestination, String jsonString) {
        HttpPost httpPost = new HttpPost(teamsDestination);
        httpPost.setEntity(new StringEntity(jsonString, StandardCharsets.UTF_8));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer broken-test-token");
        return httpPost;
    }

}