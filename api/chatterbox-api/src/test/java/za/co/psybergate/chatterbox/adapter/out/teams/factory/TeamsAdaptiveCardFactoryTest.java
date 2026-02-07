package za.co.psybergate.chatterbox.adapter.out.teams.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.psybergate.chatterbox.adapter.out.teams.model.TeamsAdaptiveCardDefinition;
import za.co.psybergate.chatterbox.adapter.out.teams.model.TeamsAdaptiveCardDefinition.Attachment.BodyItem;
import za.co.psybergate.chatterbox.application.common.template.RegexTemplateSubstitutor;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxDeliveryTeamsProperties;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamsAdaptiveCardFactoryTest {

    private final JsonFileReader jsonFileReader = new JsonFileReader();

    private final JacksonJsonConverter jsonConverter = new JacksonJsonConverter();

    private final RegexTemplateSubstitutor substitutionService = new RegexTemplateSubstitutor();

    @Mock
    private ChatterboxDeliveryTeamsProperties teamsProperties;

    @Mock
    private OutboundEventMapperPort eventExtractor;

    private TeamsAdaptiveCardFactory teamsPayloadFactory;

    @BeforeEach
    void setUp() throws Exception {
        teamsPayloadFactory = new TeamsAdaptiveCardFactory(teamsProperties, substitutionService);

        Field jsonConverterField = JsonFileReader.class.getDeclaredField("jsonConverter");
        jsonConverterField.setAccessible(true);
        jsonConverterField.set(jsonFileReader, jsonConverter);

        // Setup default template in properties
        TeamsAdaptiveCardDefinition template = createDefaultTemplate();
        when(teamsProperties.getAdaptiveCardDefinition()).thenReturn(template);
    }

    private TeamsAdaptiveCardDefinition createDefaultTemplate() {
        TeamsAdaptiveCardDefinition definition = new TeamsAdaptiveCardDefinition();
        TeamsAdaptiveCardDefinition.Attachment attachment = new TeamsAdaptiveCardDefinition.Attachment();
        TeamsAdaptiveCardDefinition.Attachment.Content content = new TeamsAdaptiveCardDefinition.Attachment.Content();
        TeamsAdaptiveCardDefinition.Attachment.BodyItem bodyItem = new TeamsAdaptiveCardDefinition.Attachment.BodyItem();
        bodyItem.setText("Push Event for ${repositoryName}");
        content.setBody(List.of(bodyItem));
        attachment.setContent(content);
        definition.setAttachments(List.of(attachment));
        return definition;
    }

    @DisplayName("Factory(Map) can build template")
    @Test
    void givenProperties_WhenTemplateIsBuilt_ThenSuccess() {
        Map<String, String> propertiesToUse = getPropertiesToUse();
        TeamsAdaptiveCardDefinition teamsAdaptiveCardDefinition = teamsPayloadFactory.buildDefinition(propertiesToUse);

        assertNotNull(teamsAdaptiveCardDefinition);
        List<BodyItem> bodyItems = teamsAdaptiveCardDefinition.getAttachments().getFirst().getContent().getBody();
        for (var bodyItem : bodyItems) {
            assertFalse(bodyItem.getText().contains("${}"));
            assertTrue(bodyItem.getText().contains("psyAlexBlakeGoudemond/chatterbox"));
        }
    }

    @DisplayName("Factory(DTO) can build template")
    @Test
    void givenGithubEventDto_WhenBuildTeamsAdaptiveCard_ThenSuccess() {
        OutboundEvent outboundEvent = createOutboundEventForComparison();
        TeamsAdaptiveCardDefinition teamsAdaptiveCardDefinition = teamsPayloadFactory.buildDefinition(outboundEvent);

        assertNotNull(teamsAdaptiveCardDefinition);
        List<TeamsAdaptiveCardDefinition.Attachment.BodyItem> bodyItems = teamsAdaptiveCardDefinition.getAttachments().getFirst().getContent().getBody();
        for (var bodyItem : bodyItems) {
            assertFalse(bodyItem.getText().contains("${}"));
        }
    }

    @DisplayName("Template --> JSON")
    @Test
    void whenCompareTemplate_ToJson_ThenIdentical() {
        TeamsAdaptiveCardDefinition templateForComparison = createTemplateForComparison();
        when(teamsProperties.getAdaptiveCardDefinition()).thenReturn(templateForComparison);

        OutboundEvent outboundEvent = createOutboundEventForComparison();
        TeamsAdaptiveCardDefinition teamsAdaptiveCardDefinition = teamsPayloadFactory.buildDefinition(outboundEvent);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualJson = objectMapper.valueToTree(teamsAdaptiveCardDefinition);
        JsonNode expectedJson = jsonFileReader.getTeamsPayloadValid();

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

    private TeamsAdaptiveCardDefinition createTemplateForComparison() {
        TeamsAdaptiveCardDefinition definition = new TeamsAdaptiveCardDefinition();
        TeamsAdaptiveCardDefinition.Attachment attachment = new TeamsAdaptiveCardDefinition.Attachment();
        TeamsAdaptiveCardDefinition.Attachment.Content content = new TeamsAdaptiveCardDefinition.Attachment.Content();

        TeamsAdaptiveCardDefinition.Attachment.MsTeams msTeams = new TeamsAdaptiveCardDefinition.Attachment.MsTeams();
        content.setMsteams(msTeams);

        TeamsAdaptiveCardDefinition.Attachment.BodyItem item1 = new TeamsAdaptiveCardDefinition.Attachment.BodyItem();
        item1.setText("📢 ${displayName} for ${repositoryName}");

        TeamsAdaptiveCardDefinition.Attachment.BodyItem item2 = new TeamsAdaptiveCardDefinition.Attachment.BodyItem();
        item2.setText("⁉️ Details: [${urlDisplayText}](${url}) created by ${senderName}");

        content.setBody(List.of(item1, item2));
        attachment.setContent(content);
        definition.setAttachments(List.of(attachment));
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