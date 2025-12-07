package za.co.psybergate.chatterbox.application.webhook.factory;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.domain.utility.ConversionUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//        (classes = {
//        TemplateSubstitutionService.class,
//        TeamsCardFactory.class,
//        ConversionUtilitiesImpl.class,
//        TeamsAdaptiveCardTemplateProperties.class,
//        ApplicationConfig.class,
//})
public class TeamsCardFactoryIT {

    @Autowired
    private TeamsCardFactory teamsCardFactory;

    @Autowired
    private ConversionUtilities conversionUtilities;

    @DisplayName("Factory can build template")
    @Test
    public void givenProperties_WhenTemplateIsBuilt_ThenSuccess() {
        Map<String, String> propertiesToUse = getPropertiesToUse();

        TeamsAdaptiveCardTemplate teamsAdaptiveCardTemplate = null;
        try {
            teamsAdaptiveCardTemplate = teamsCardFactory.buildCard(propertiesToUse);
        } catch (Exception e) {
            fail("Expected the TeamsCardFactory to be able to build an TeamsAdaptiveCardTemplate");
        }

        assertNotNull(teamsAdaptiveCardTemplate);
        List<TeamsAdaptiveCardTemplate.BodyItem> bodyItems = teamsAdaptiveCardTemplate.getAttachments().getFirst().getContent().getBody();
        for (var bodyItem : bodyItems) {
            assertFalse(bodyItem.getText().contains("${}"));
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

    private String readExampleTeamsPayload() {
        String pathToFile = "src/test/resources/payload/teams-payload-valid.json";
        return conversionUtilities.readPayload(pathToFile);
    }

}