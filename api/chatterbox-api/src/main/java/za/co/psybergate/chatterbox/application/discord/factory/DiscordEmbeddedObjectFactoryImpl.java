package za.co.psybergate.chatterbox.application.discord.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryDiscordProperties;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryDiscordProperties.EmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.infrastructure.http.HttpResponseHandler;
import za.co.psybergate.chatterbox.infrastructure.template.TemplateSubstitutor;
import za.co.psybergate.chatterbox.infrastructure.template.TemplateSubstitutorImpl;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DiscordEmbeddedObjectFactoryImpl implements DiscordEmbeddedObjectFactory {

    private final ChatterboxDeliveryDiscordProperties discordProperties;

    private final TemplateSubstitutor substitutionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpResponseHandler httpResponseHandler;

    @Override
    public EmbeddedObjectDefinition buildEmbeddedObjectDefinition(Map<String, String> values) {
        EmbeddedObjectDefinition clone = deepCopy(discordProperties.getEmbeddedObjectDefinition()); // use Jackson

        clone.getEmbeds().forEach(embeddedObject -> {
            embeddedObject.setTitle(
                    substitutionService.apply(embeddedObject.getTitle(), values)
            );
            embeddedObject.setDescription(
                    substitutionService.apply(embeddedObject.getDescription(), values)
            );
            embeddedObject.setUrl(
                    substitutionService.apply(embeddedObject.getUrl(), values)
            );
            embeddedObject.getAuthor().setName(
                    substitutionService.apply(embeddedObject.getAuthor().getName(), values)
            );
        });

        return clone;
    }

    @Override
    public EmbeddedObjectDefinition buildEmbeddedObjectDefinition(GithubEventDto dto) {
        Map<String, String> values = Map.of(
                "displayName", dto.displayName(),
                "repositoryName", dto.repositoryName(),
                "senderName", dto.senderName(),
                "url", dto.url(),
                "urlDisplayText", dto.urlDisplayText()
        );
        return buildEmbeddedObjectDefinition(values);
    }

    @Override
    public String getAsDiscordPayloadString(GithubEventDto eventDto) throws ApplicationException {
        EmbeddedObjectDefinition embeddedObjectDefinition = buildEmbeddedObjectDefinition(eventDto);
        String teamsPayload;
        try {
            teamsPayload = objectMapper.writeValueAsString(embeddedObjectDefinition);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Unexpected issue when converting EventDto to Json String", e);
        }
        return teamsPayload;
    }

    @Override
    public HttpResponseDto getHttpResponseDto(ClassicHttpResponse response) {
        return httpResponseHandler.getHttpResponseDto(response);
    }

    private EmbeddedObjectDefinition deepCopy(EmbeddedObjectDefinition src) {
        return objectMapper.convertValue(src, EmbeddedObjectDefinition.class);
    }

}
