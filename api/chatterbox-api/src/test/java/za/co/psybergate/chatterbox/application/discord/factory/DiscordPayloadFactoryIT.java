package za.co.psybergate.chatterbox.application.discord.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.adapter.out.discord.factory.DiscordEmbeddedObjectFactoryPort;
import za.co.psybergate.chatterbox.application.common.template.RegexTemplateSubstitutor;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import za.co.psybergate.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.adapter.out.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.adapter.out.discord.factory.DiscordPayloadFactory;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        DiscordPayloadFactory.class,
        InfrastructurePropertiesConfig.class,
        RegexTemplateSubstitutor.class,
        HttpResponseHandler.class,
        JsonFileReader.class,
        GithubWebhookEventMapper.class,
        PropertiesConfigurationResolver.class,
        JacksonJsonConverter.class,
})
public class DiscordPayloadFactoryIT {

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private DiscordEmbeddedObjectFactoryPort discordEmbeddedObjectFactoryPort;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private OutboundEventMapperPort eventExtractor;

    @DisplayName("Factory(DTO) can build template")
    @Test
    public void givenGithubEventDto_WhenBuildDiscordEmbeddedObjectFactory_ThenSuccess() {
        DiscordEmbeddedObjectDefinition embeddedObjectDefinition = getDiscordEmbeddedObjectTemplateUsingMap();
        if (embeddedObjectDefinition == null) {
            fail("Expected the DiscordFactory to be able to build a Template");
        }

        assertNotNull(embeddedObjectDefinition);
        List<DiscordEmbeddedObjectDefinition.EmbeddedObject> bodyItems = embeddedObjectDefinition.getEmbeds();
        for (var bodyItem : bodyItems) {
            assertFalse(bodyItem.getTitle().contains("${}"));
            assertFalse(bodyItem.getDescription().contains("${}"));
            assertFalse(bodyItem.getUrl().contains("${}"));
            assertFalse(bodyItem.getAuthor().getName().contains("${}"));
        }
    }

    @DisplayName("Template --> JSON")
    @Test
    public void whenCompareTemplate_ToJson_ThenIdentical() {
        DiscordEmbeddedObjectDefinition embeddedObjectDefinition = getDiscordEmbeddedObjectTemplateUsingJsonString();
        if (embeddedObjectDefinition == null) {
            fail("Expected the DiscordFactory to be able to build a Template");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualJson = objectMapper.valueToTree(embeddedObjectDefinition);
        JsonNode expectedJson = jsonFileReader.getDiscordPayloadValid();

        assertEquals(expectedJson, actualJson);
    }

    private DiscordEmbeddedObjectDefinition getDiscordEmbeddedObjectTemplateUsingJsonString() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        OutboundEvent outboundEvent = eventExtractor.map(WebhookEventType.PUSH, jsonNode);
        try {
            return (DiscordEmbeddedObjectDefinition) discordEmbeddedObjectFactoryPort.buildEmbeddedObjectDefinition(outboundEvent);
        } catch (Exception e) {
            return null;
        }
    }

    private DiscordEmbeddedObjectDefinition getDiscordEmbeddedObjectTemplateUsingMap() {
        Map<String, String> propertiesToUse = getPropertiesToUse();
        try {
            return (DiscordEmbeddedObjectDefinition) discordEmbeddedObjectFactoryPort.buildEmbeddedObjectDefinition(propertiesToUse);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, String> getPropertiesToUse() {
        Map<String, String> propertiesToUse = new HashMap<>();
        propertiesToUse.put("repositoryName", "psyAlexBlakeGoudemond/chatterbox");
        propertiesToUse.put("senderName", "psyAlexBlakeGoudemond");
        propertiesToUse.put("url", "http://localhost:abcd");
        propertiesToUse.put("urlDisplayText", "Test message Is here!");
        propertiesToUse.put("displayName", "Pull Request Event");
        return propertiesToUse;
    }


}