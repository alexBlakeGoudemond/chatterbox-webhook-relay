package za.co.psybergate.chatterbox.application.discord.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryDiscordProperties;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// TODO BlakeGoudemond 2026/01/09 | reduce scope
@SpringBootTest
public class DiscordEmbeddedObjectFactoryImplIT {

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private DiscordEmbeddedObjectFactory discordEmbeddedObjectFactory;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @DisplayName("Factory(DTO) can build template")
    @Test
    public void givenGithubEventDto_WhenBuildDiscordEmbeddedObjectFactory_ThenSuccess() {
        ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition embeddedObjectDefinition = getDiscordEmbeddedObjectTemplateUsingMap();
        if (embeddedObjectDefinition == null) {
            fail("Expected the DiscordFactory to be able to build a Template");
        }

        assertNotNull(embeddedObjectDefinition);
        List<ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition.EmbeddedObject> bodyItems = embeddedObjectDefinition.getEmbeds();
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
        ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition embeddedObjectDefinition = getDiscordEmbeddedObjectTemplateUsingJsonString();
        if (embeddedObjectDefinition == null) {
            fail("Expected the DiscordFactory to be able to build a Template");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualJson = objectMapper.valueToTree(embeddedObjectDefinition);
        JsonNode expectedJson = jsonFileReader.getDiscordPayloadValid();

        assertEquals(expectedJson, actualJson);
    }

    private ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition getDiscordEmbeddedObjectTemplateUsingJsonString() {
        JsonNode jsonNode = jsonFileReader.getGithubPayloadValid();
        GithubEventDto eventDto = eventExtractor.extract(EventType.PUSH, jsonNode);
        try {
            return discordEmbeddedObjectFactory.buildEmbeddedObjectDefinition(eventDto);
        } catch (Exception e) {
            return null;
        }
    }

    private ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition getDiscordEmbeddedObjectTemplateUsingMap() {
        Map<String, String> propertiesToUse = getPropertiesToUse();
        try {
            return discordEmbeddedObjectFactory.buildEmbeddedObjectDefinition(propertiesToUse);
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