package za.co.psybergate.chatterbox.adapter.out.discord.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.psybergate.chatterbox.adapter.out.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.application.common.template.RegexTemplateSubstitutor;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxDeliveryDiscordProperties;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscordEmbeddedObjectFactoryTest {

    private final JsonFileReader jsonFileReader = new JsonFileReader();

    private final JacksonJsonConverter jsonConverter = new JacksonJsonConverter();

    private final RegexTemplateSubstitutor substitutionService = new RegexTemplateSubstitutor();

    @Mock
    private ChatterboxDeliveryDiscordProperties discordProperties;

    private DiscordEmbeddedObjectFactory discordPayloadFactory;

    @BeforeEach
    void setUp() throws Exception {
        discordPayloadFactory = new DiscordEmbeddedObjectFactory(discordProperties, substitutionService);

        Field jsonConverterField = JsonFileReader.class.getDeclaredField("jsonConverter");
        jsonConverterField.setAccessible(true);
        jsonConverterField.set(jsonFileReader, jsonConverter);

        DiscordEmbeddedObjectDefinition template = createDefaultTemplate();
        when(discordProperties.getEmbeddedObjectDefinition()).thenReturn(template);
    }

    private DiscordEmbeddedObjectDefinition createDefaultTemplate() {
        DiscordEmbeddedObjectDefinition definition = new DiscordEmbeddedObjectDefinition();
        DiscordEmbeddedObjectDefinition.EmbeddedObject embed = new DiscordEmbeddedObjectDefinition.EmbeddedObject();
        embed.setTitle("Title ${displayName}");
        embed.setDescription("Description ${repositoryName}");
        embed.setUrl("${url}");
        DiscordEmbeddedObjectDefinition.EmbeddedObject.Author author = new DiscordEmbeddedObjectDefinition.EmbeddedObject.Author();
        author.setName("${senderName}");
        embed.setAuthor(author);
        definition.setEmbeds(List.of(embed));
        return definition;
    }

    @DisplayName("Factory(DTO) can build template")
    @Test
    void givenGithubEventDto_WhenBuildDiscordEmbeddedObjectFactory_ThenSuccess() {
        OutboundEvent outboundEvent = createOutboundEventForComparison();
        DiscordEmbeddedObjectDefinition embeddedObjectDefinition = discordPayloadFactory.buildDefinition(outboundEvent);

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
    void whenCompareTemplate_ToJson_ThenIdentical() {
        DiscordEmbeddedObjectDefinition templateForComparison = createTemplateForComparison();
        when(discordProperties.getEmbeddedObjectDefinition()).thenReturn(templateForComparison);

        OutboundEvent outboundEvent = createOutboundEventForComparison();
        DiscordEmbeddedObjectDefinition embeddedObjectDefinition = discordPayloadFactory.buildDefinition(outboundEvent);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualJson = objectMapper.valueToTree(embeddedObjectDefinition);
        JsonNode expectedJson = jsonFileReader.getDiscordPayloadValid();

        assertEquals(expectedJson, actualJson);
    }

    private OutboundEvent createOutboundEventForComparison() {
        return new OutboundEvent(
                1L, "source-123", "PUSH", "Push Event (chatterbox)",
                "psyAlexBlakeGoudemond/chatterbox", "psyAlexBlakeGoudemond",
                "https://github.com/psyAlexBlakeGoudemond/chatterbox/blob/develop/api/chatterbox-api/chattering_teeth.gif", "Test message Is here!",
                "extra", "{}"
        );
    }

    private DiscordEmbeddedObjectDefinition createTemplateForComparison() {
        // Matching discord-payload-valid.json
        DiscordEmbeddedObjectDefinition definition = new DiscordEmbeddedObjectDefinition();
        DiscordEmbeddedObjectDefinition.EmbeddedObject embed = new DiscordEmbeddedObjectDefinition.EmbeddedObject();
        embed.setTitle("${urlDisplayText}");
        embed.setDescription("📢 ${displayName} for ${repositoryName}");
        embed.setUrl("${url}");
        embed.setColor(6993);
        DiscordEmbeddedObjectDefinition.EmbeddedObject.Author author = new DiscordEmbeddedObjectDefinition.EmbeddedObject.Author();
        author.setName("${senderName}");
        author.setIcon_url("https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png");
        embed.setAuthor(author);
        definition.setEmbeds(List.of(embed));
        return definition;
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