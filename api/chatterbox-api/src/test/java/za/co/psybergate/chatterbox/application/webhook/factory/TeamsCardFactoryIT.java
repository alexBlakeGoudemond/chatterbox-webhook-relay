package za.co.psybergate.chatterbox.application.webhook.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.webhook.service.TemplateSubstitutionService;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.domain.utility.ConversionUtilities;
import za.co.psybergate.chatterbox.domain.utility.ConversionUtilitiesImpl;
import za.co.psybergate.chatterbox.infrastructure.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        TeamsCardFactory.class,
        TeamsAdaptiveCardTemplate.class,
        TemplateSubstitutionService.class,
        ApplicationConfig.class,
        ConversionUtilitiesImpl.class,
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

    @DisplayName("Factory can build template")
    @Test
    public void givenProperties_WhenTemplateIsBuilt_ThenSuccess() {
        Map<String, String> propertiesToUse = getPropertiesToUse();

        TeamsAdaptiveCardTemplate teamsAdaptiveCardTemplate = null;
        try {
            teamsAdaptiveCardTemplate = teamsCardFactory.buildCard(propertiesToUse);
        } catch (Exception e) {
            fail("Expected the TeamsCardFactory to be able to build an TeamsAdaptiveCardTemplate");
            return;
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