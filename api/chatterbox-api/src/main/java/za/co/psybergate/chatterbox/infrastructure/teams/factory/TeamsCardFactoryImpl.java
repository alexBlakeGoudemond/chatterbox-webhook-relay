package za.co.psybergate.chatterbox.infrastructure.teams.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.teams.factory.TeamsCardFactory;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryTeamsProperties;
import za.co.psybergate.chatterbox.domain.teams.TeamsAdaptiveCardDefinition;
import za.co.psybergate.chatterbox.infrastructure.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.template.TemplateSubstitutorImpl;

import java.util.Map;

import static za.co.psybergate.chatterbox.domain.github.GithubEventMapping.GithubIncomingMappingFieldKeys.*;

@Component
@RequiredArgsConstructor
public class TeamsCardFactoryImpl implements TeamsCardFactory {

    private final ChatterboxDeliveryTeamsProperties teamsProperties;

    private final TemplateSubstitutorImpl substitutionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpResponseHandler httpResponseHandler;

    @Override
    public TeamsAdaptiveCardDefinition buildCard(Map<String, String> values) {
        TeamsAdaptiveCardDefinition clone = deepCopy(teamsProperties.getAdaptiveCardDefinition()); // use Jackson

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

    @Override
    public TeamsAdaptiveCardDefinition buildCard(GithubEventDto dto) {
        Map<String, String> values = Map.of(
                "displayName", dto.displayName(),
                REPOSITORYNAME.getFieldName(), dto.repositoryName(),
                SENDERNAME.getFieldName(), dto.senderName(),
                URL.getFieldName(), dto.url(),
                URLDISPLAYTEXT.getFieldName(), dto.urlDisplayText(),
                EXTRADETAIL.getFieldName(), dto.extraDetail()
        );
        return buildCard(values);
    }

    private TeamsAdaptiveCardDefinition deepCopy(TeamsAdaptiveCardDefinition src) {
        return objectMapper.convertValue(src, TeamsAdaptiveCardDefinition.class);
    }

    @Override
    public String getAsTeamsPayloadString(GithubEventDto eventDto) throws ApplicationException {
        TeamsAdaptiveCardDefinition teamsAdaptiveCardDefinition = buildCard(eventDto);
        String teamsPayload;
        try {
            teamsPayload = objectMapper.writeValueAsString(teamsAdaptiveCardDefinition);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Unexpected issue when converting EventDto to Json String", e);
        }
        return teamsPayload;
    }

    @Override
    public HttpResponseDto getHttpResponseDto(ClassicHttpResponse response) {
        return httpResponseHandler.getHttpResponseDto(response);
    }

}

