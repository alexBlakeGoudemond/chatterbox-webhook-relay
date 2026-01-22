package za.co.psybergate.chatterbox.application.discord.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.usecase.discord.factory.DiscordEmbeddedObjectFactory;
import za.co.psybergate.chatterbox.application.usecase.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.usecase.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapperImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.adapter.discord.factory.DiscordEmbeddedObjectFactoryImpl;
import za.co.psybergate.chatterbox.infrastructure.out.config.ApplicationPropertiesConfig;
import za.co.psybergate.chatterbox.infrastructure.in.web.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.out.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.out.webhook.resolution.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        DiscordEmbeddedObjectFactoryImpl.class,
        ApplicationPropertiesConfig.class,
        TemplateSubstitutorImpl.class,
        HttpResponseHandler.class,
        JsonFileReader.class,
        GithubEventMapperImpl.class,
        WebhookConfigurationResolverImpl.class,
        JsonConverterImpl.class,
})
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
    private GithubEventMapper eventExtractor;

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
        GithubEventDto eventDto = eventExtractor.map(EventType.PUSH, jsonNode);
        try {
            return discordEmbeddedObjectFactory.buildEmbeddedObjectDefinition(eventDto);
        } catch (Exception e) {
            return null;
        }
    }

    private DiscordEmbeddedObjectDefinition getDiscordEmbeddedObjectTemplateUsingMap() {
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