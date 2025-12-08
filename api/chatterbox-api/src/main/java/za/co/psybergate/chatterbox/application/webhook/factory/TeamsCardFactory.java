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

    /// From a given [Map] of property values, create and populate the
    /// [TeamsAdaptiveCardTemplate]
    public TeamsAdaptiveCardTemplate buildCard(Map<String, String> values) {
        TeamsAdaptiveCardTemplate clone = deepCopy(template); // use Jackson

        clone.getAttachments().forEach(attachment -> {
            var content = attachment.getContent();

            content.getBody().forEach(bodyItem -> {
                String textAsJsonKey = bodyItem.getText();
                bodyItem.setText(
                        substitutionService.apply(textAsJsonKey, values)
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

