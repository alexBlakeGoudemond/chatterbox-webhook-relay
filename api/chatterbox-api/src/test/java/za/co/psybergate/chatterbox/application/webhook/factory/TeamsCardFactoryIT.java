package za.co.psybergate.chatterbox.application.webhook.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.webhook.extractor.GithubEventExtractor;
import za.co.psybergate.chatterbox.application.webhook.resolver.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.application.webhook.service.TemplateSubstitutionServiceImpl;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.domain.utility.ConversionUtilities;
import za.co.psybergate.chatterbox.domain.utility.ConversionUtilitiesImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        TeamsCardFactory.class,
        TeamsAdaptiveCardTemplate.class,
        TemplateSubstitutionServiceImpl.class,
        ApplicationConfig.class,
        ConversionUtilitiesImpl.class,
        GithubEventExtractor.class,
        WebhookConfigurationResolverImpl.class,
        WebhookLogger.class,
})
public class TeamsCardFactoryIT {

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private TeamsCardFactory teamsCardFactory;

    @Autowired
    private ConversionUtilities conversionUtilities;

    @Autowired
    private GithubEventExtractor eventExtractor;

    @DisplayName("Factory(Map) can build template")
    @Test
    public void givenProperties_WhenTemplateIsBuilt_ThenSuccess() {
        TeamsAdaptiveCardTemplate teamsAdaptiveCardTemplate = getTeamsAdaptiveCardTemplateUsingMap();
        if (teamsAdaptiveCardTemplate == null) {
            fail("Expected the TeamsCardFactory to be able to build an TeamsAdaptiveCardTemplate");
        }
        ;

        assertNotNull(teamsAdaptiveCardTemplate);
        List<TeamsAdaptiveCardTemplate.BodyItem> bodyItems = teamsAdaptiveCardTemplate.getAttachments().getFirst().getContent().getBody();
        for (var bodyItem : bodyItems) {
            assertFalse(bodyItem.getText().contains("${}"));
        }
    }

    @DisplayName("Factory(DTO) can build template")
    @Test
    public void givenGithubEventDto_WhenBuildTeamsAdaptiveCard_ThenSuccess() {
        TeamsAdaptiveCardTemplate teamsAdaptiveCardTemplate = getTeamsAdaptiveCardTemplateFromJsonString();
        if (teamsAdaptiveCardTemplate == null) {
            fail("Expected the TeamsCardFactory to be able to build an TeamsAdaptiveCardTemplate");
        }

        assertNotNull(teamsAdaptiveCardTemplate);
        List<TeamsAdaptiveCardTemplate.BodyItem> bodyItems = teamsAdaptiveCardTemplate.getAttachments().getFirst().getContent().getBody();
        for (var bodyItem : bodyItems) {
            assertFalse(bodyItem.getText().contains("${}"));
        }
    }

    @DisplayName("Template --> JSON")
    @Test
    public void whenCompareTemplate_ToJson_ThenIdentical() {
        TeamsAdaptiveCardTemplate teamsAdaptiveCardTemplate = getTeamsAdaptiveCardTemplateFromJsonString();
        if (teamsAdaptiveCardTemplate == null) {
            fail("Expected the TeamsCardFactory to be able to build an TeamsAdaptiveCardTemplate");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualJson = objectMapper.valueToTree(teamsAdaptiveCardTemplate);
        JsonNode expectedJson = null;
        try {
            expectedJson = objectMapper.readTree(exampleTeamsPayload());
        } catch (JsonProcessingException e) {
            fail("Unexpected issue when converting JsonString into JsonNode", e);
        }

        assertEquals(expectedJson, actualJson);
    }

    private TeamsAdaptiveCardTemplate getTeamsAdaptiveCardTemplateFromJsonString() {
        JsonNode jsonNode = conversionUtilities.getAsJson(getValidJsonString());
        GithubEventDto eventDto = eventExtractor.extract("push", jsonNode);
        try {
            return teamsCardFactory.buildCard(eventDto);
        } catch (Exception e) {
            return null;
        }
    }

    private TeamsAdaptiveCardTemplate getTeamsAdaptiveCardTemplateUsingMap() {
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

    private String exampleTeamsPayload() {
        String pathToFile = "src/test/resources/payload/teams-payload-valid.json";
        return conversionUtilities.readPayload(pathToFile);
    }

    private String getValidJsonString() {
        String pathToFile = "src/test/resources/payload/github-payload-valid.json";
        return conversionUtilities.readPayload(pathToFile);
    }

}