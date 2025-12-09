package za.co.psybergate.chatterbox.application.webhook.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.webhook.service.TemplateSubstitutionServiceImpl;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.infrastructure.config.properties.TeamsAdaptiveCardTemplateProperties;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TeamsCardFactoryImpl implements TeamsCardFactory {

    private final TeamsAdaptiveCardTemplateProperties template;

    private final TemplateSubstitutionServiceImpl substitutionService;

    /// From a given [Map] of property values, create and populate the
    /// [TeamsAdaptiveCardTemplate]
    @Override
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

    /// From a given [GithubEventDto] create a [Map] and leverage [TeamsCardFactoryImpl#buildCard(Map)]
    /// to create a [TeamsAdaptiveCardTemplate]
    @Override
    public TeamsAdaptiveCardTemplate buildCard(GithubEventDto dto) {
        Map<String, String> values = Map.of(
                "displayName", dto.displayName(),
                "repositoryName", dto.repositoryName(),
                "senderName", dto.senderName(),
                "url", dto.url(),
                "urlDisplayText", dto.urlDisplayText()
        );
        return buildCard(values);
    }

    private TeamsAdaptiveCardTemplate deepCopy(TeamsAdaptiveCardTemplate src) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(src, TeamsAdaptiveCardTemplate.class);
    }

}

