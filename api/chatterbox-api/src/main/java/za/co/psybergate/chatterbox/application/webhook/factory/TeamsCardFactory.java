package za.co.psybergate.chatterbox.application.webhook.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.webhook.service.TemplateSubstitutionService;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.infrastructure.config.properties.TeamsAdaptiveCardTemplateProperties;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamsCardFactory {

    private final TeamsAdaptiveCardTemplateProperties template;

    private final TemplateSubstitutionService substitutionService;

    // TODO BlakeGoudemond 2025/12/07 | Add Javadoc
    public TeamsAdaptiveCardTemplate buildCard(Map<String, String> values) {
        TeamsAdaptiveCardTemplate clone = deepCopy(template); // use Jackson

        clone.getAttachments().forEach(att -> {
            TeamsAdaptiveCardTemplate.Content content = att.getContent();

            content.getBody().forEach(body -> {
                String original = body.getText();
                body.setText(
                        substitutionService.apply(original, values)
                );
            });
        });

        return clone;
    }

    private TeamsAdaptiveCardTemplate deepCopy(TeamsAdaptiveCardTemplate src) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(src, TeamsAdaptiveCardTemplate.class);
    }

}

