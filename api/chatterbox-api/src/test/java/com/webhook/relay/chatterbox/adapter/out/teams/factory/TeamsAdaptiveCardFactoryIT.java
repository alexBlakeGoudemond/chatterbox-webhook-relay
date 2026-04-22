package com.webhook.relay.chatterbox.adapter.out.teams.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.webhook.relay.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import com.webhook.relay.chatterbox.adapter.in.web.filter.WebhookFilter;
import com.webhook.relay.chatterbox.adapter.out.http.HttpResponseHandler;
import com.webhook.relay.chatterbox.adapter.out.teams.model.TeamsAdaptiveCardDefinition;
import com.webhook.relay.chatterbox.adapter.out.teams.model.TeamsAdaptiveCardDefinition.Attachment.BodyItem;
import com.webhook.relay.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import com.webhook.relay.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import com.webhook.relay.chatterbox.application.common.logging.slf4j.Slf4jWebhookLogger;
import com.webhook.relay.chatterbox.application.common.template.RegexTemplateSubstitutor;
import com.webhook.relay.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.port.out.vendor.factory.VendorFactoryPort;
import com.webhook.relay.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import com.webhook.relay.chatterbox.common.config.InfrastructurePropertiesConfig;
import com.webhook.relay.chatterbox.common.logging.convenience.ImportSlf4jWebhookLogger;
import com.webhook.relay.chatterbox.test.helper.JsonFileReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ImportSlf4jWebhookLogger
@SpringBootTest(classes = {
        TeamsAdaptiveCardFactory.class,
        RegexTemplateSubstitutor.class,
        InfrastructurePropertiesConfig.class,
        JsonFileReader.class,
        JacksonJsonConverter.class,
        GithubWebhookEventMapper.class,
        PropertiesConfigurationResolver.class,
        Slf4jWebhookLogger.class,
        HttpResponseHandler.class
})
public class TeamsAdaptiveCardFactoryIT {

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    @Qualifier("teamsAdaptiveCardFactory")
    private VendorFactoryPort teamsPayloadFactory;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private OutboundEventMapperPort eventExtractor;

    @DisplayName("Factory(Map) can build template")
    @Test
    public void givenProperties_WhenTemplateIsBuilt_ThenSuccess() {
        TeamsAdaptiveCardDefinition teamsAdaptiveCardDefinition = getTeamsAdaptiveCardTemplateUsingMap();
        if (teamsAdaptiveCardDefinition == null) {
            fail("Expected the TeamsCardFactory to be able to build an TeamsAdaptiveCardTemplate");
        }

        assertNotNull(teamsAdaptiveCardDefinition);
        List<BodyItem> bodyItems = teamsAdaptiveCardDefinition.getAttachments().getFirst().getContent().getBody();
        for (var bodyItem : bodyItems) {
            assertFalse(bodyItem.getText().contains("${}"));
        }
    }

    @DisplayName("Factory(DTO) can build template")
    @Test
    public void givenGithubEventDto_WhenBuildTeamsAdaptiveCard_ThenSuccess() {
        TeamsAdaptiveCardDefinition teamsAdaptiveCardDefinition = getTeamsAdaptiveCardTemplateFromJsonString();
        if (teamsAdaptiveCardDefinition == null) {
            fail("Expected the Factory to be able to build a Template");
        }

        assertNotNull(teamsAdaptiveCardDefinition);
        List<TeamsAdaptiveCardDefinition.Attachment.BodyItem> bodyItems = teamsAdaptiveCardDefinition.getAttachments().getFirst().getContent().getBody();
        for (var bodyItem : bodyItems) {
            assertFalse(bodyItem.getText().contains("${}"));
        }
    }

    @DisplayName("Template --> JSON")
    @Test
    public void whenCompareTemplate_ToJson_ThenIdentical() {
        TeamsAdaptiveCardDefinition teamsAdaptiveCardDefinition = getTeamsAdaptiveCardTemplateFromJsonString();
        if (teamsAdaptiveCardDefinition == null) {
            fail("Expected the TeamsCardFactory to be able to build an TeamsAdaptiveCardTemplate");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualJson = objectMapper.valueToTree(teamsAdaptiveCardDefinition);
        JsonNode expectedJson = jsonFileReader.getTeamsPayloadValid();

        assertEquals(expectedJson, actualJson);
    }

    private TeamsAdaptiveCardDefinition getTeamsAdaptiveCardTemplateFromJsonString() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, RawEventPayload.of(jsonNode));
        try {
            return (TeamsAdaptiveCardDefinition) teamsPayloadFactory.buildDefinition(outboundEvent);
        } catch (Exception e) {
            return null;
        }
    }

    private TeamsAdaptiveCardDefinition getTeamsAdaptiveCardTemplateUsingMap() {
        Map<String, String> propertiesToUse = getPropertiesToUse();
        try {
            return (TeamsAdaptiveCardDefinition) teamsPayloadFactory.buildDefinition(propertiesToUse);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, String> getPropertiesToUse() {
        Map<String, String> propertiesToUse = new HashMap<>();
        propertiesToUse.put("repositoryName", "alexBlakeGoudemond/chatterbox-webhook-relay");
        propertiesToUse.put("senderName", "alexBlakeGoudemond");
        propertiesToUse.put("url", "http://localhost:abcd");
        propertiesToUse.put("urlDisplayText", "Test message Is here!");
        propertiesToUse.put("displayName", "Pull Request Event");
        return propertiesToUse;
    }


}