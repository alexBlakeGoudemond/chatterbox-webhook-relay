package za.co.psybergate.chatterbox.application.teams.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.usecase.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.application.usecase.teams.factory.TeamsCardFactory;
import za.co.psybergate.chatterbox.application.usecase.template.TemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.application.usecase.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapperImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.teams.model.TeamsAdaptiveCardDefinition;
import za.co.psybergate.chatterbox.domain.teams.model.TeamsAdaptiveCardDefinition.Attachment.BodyItem;
import za.co.psybergate.chatterbox.infrastructure.adapter.teams.factory.TeamsCardFactoryImpl;
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
        TeamsCardFactoryImpl.class,
        TemplateSubstitutorImpl.class,
        ApplicationPropertiesConfig.class,
        JsonFileReader.class,
        JsonConverterImpl.class,
        GithubEventMapperImpl.class,
        WebhookConfigurationResolverImpl.class,
        WebhookLoggerImpl.class,
        HttpResponseHandler.class
})
public class TeamsCardFactoryImplIT {

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private TeamsCardFactory teamsCardFactory;

    @Autowired
    private JsonFileReader jsonFileReader;

    @Autowired
    private GithubEventMapper eventExtractor;

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
        GithubEventDto eventDto = eventExtractor.map(EventType.PUSH, jsonNode);
        try {
            return teamsCardFactory.buildCard(eventDto);
        } catch (Exception e) {
            return null;
        }
    }

    private TeamsAdaptiveCardDefinition getTeamsAdaptiveCardTemplateUsingMap() {
        Map<String, String> propertiesToUse = getPropertiesToUse();
        try {
            return teamsCardFactory.buildCard(propertiesToUse);
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