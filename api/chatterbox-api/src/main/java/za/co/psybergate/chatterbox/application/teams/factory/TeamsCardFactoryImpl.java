package za.co.psybergate.chatterbox.application.teams.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.teams.factory.template.TeamsTemplateSubstitutorImpl;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryTeamsProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryTeamsProperties.TeamsAdaptiveCardTemplate;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TeamsCardFactoryImpl implements TeamsCardFactory {

    private final ChatterboxDeliveryTeamsProperties teamsProperties;

    private final TeamsTemplateSubstitutorImpl substitutionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /// From a given [Map] of property values, create and populate the
    /// [TeamsAdaptiveCardTemplate]
    @Override
    public TeamsAdaptiveCardTemplate buildCard(Map<String, String> values) {
        TeamsAdaptiveCardTemplate clone = deepCopy(teamsProperties.getAdaptiveCardTemplate()); // use Jackson

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

    @Override
    public String getAsTeamsPayloadString(GithubEventDto eventDto) throws InternalServerException {
        TeamsAdaptiveCardTemplate teamsAdaptiveCardTemplate = buildCard(eventDto);
        String teamsPayload;
        try {
            teamsPayload = objectMapper.writeValueAsString(teamsAdaptiveCardTemplate);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Unexpected issue when converting EventDto to Json String", e);
        }
        return teamsPayload;
    }

    @Override
    public HttpResponseDto getHttpResponseDto(ClassicHttpResponse response) throws InternalServerException {
        int status = response.getCode();
        String rawBody = null;
        JsonNode jsonNode = null;
        if (response.getEntity() != null) {
            rawBody = getAsString(response);
            jsonNode = getJsonNode(rawBody);
        }
        return new HttpResponseDto(status, rawBody, jsonNode);
    }

    private JsonNode getJsonNode(String rawBody) throws InternalServerException {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new InternalServerException("Unexpected issue when converting String into a JsonNode", e);
        }
    }

    private String getAsString(ClassicHttpResponse response) throws InternalServerException {
        try {
            return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InternalServerException("Unable to parse the Response Body into a String", e);
        }
    }

}

