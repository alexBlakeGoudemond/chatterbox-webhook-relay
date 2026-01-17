package za.co.psybergate.chatterbox.infrastructure.adapter.discord.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.usecase.discord.factory.DiscordEmbeddedObjectFactory;
import za.co.psybergate.chatterbox.application.usecase.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.usecase.template.TemplateSubstitutor;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.delivery.model.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxDeliveryDiscordProperties;
import za.co.psybergate.chatterbox.infrastructure.out.http.HttpResponseHandler;

import java.util.Map;

import static za.co.psybergate.chatterbox.domain.github.model.GithubEventMapping.GithubIncomingMappingFieldKeys.*;

@Component
@RequiredArgsConstructor
public class DiscordEmbeddedObjectFactoryImpl implements DiscordEmbeddedObjectFactory {

    private final ChatterboxDeliveryDiscordProperties discordProperties;

    private final TemplateSubstitutor substitutionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpResponseHandler httpResponseHandler;

    @Override
    public DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(Map<String, String> values) {
        DiscordEmbeddedObjectDefinition clone = deepCopy(discordProperties.getEmbeddedObjectDefinition()); // use Jackson

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
    public DiscordEmbeddedObjectDefinition buildEmbeddedObjectDefinition(GithubEventDto dto) {
        Map<String, String> values = Map.of(
                "displayName", dto.displayName(),
                REPOSITORYNAME.getFieldName(), dto.repositoryName(),
                SENDERNAME.getFieldName(), dto.senderName(),
                URL.getFieldName(), dto.url(),
                URLDISPLAYTEXT.getFieldName(), dto.urlDisplayText(),
                EXTRADETAIL.getFieldName(), dto.extraDetail()
        );
        return buildEmbeddedObjectDefinition(values);
    }

    @Override
    public String getAsDiscordPayloadString(GithubEventDto eventDto) throws ApplicationException {
        DiscordEmbeddedObjectDefinition embeddedObjectDefinition = buildEmbeddedObjectDefinition(eventDto);
        String teamsPayload;
        try {
            teamsPayload = objectMapper.writeValueAsString(embeddedObjectDefinition);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Unexpected issue when converting EventDto to Json String", e);
        }
        return teamsPayload;
    }

    public HttpResponseDto getHttpResponseDto(ClassicHttpResponse response) {
        return httpResponseHandler.getHttpResponseDto(response);
    }

    private DiscordEmbeddedObjectDefinition deepCopy(DiscordEmbeddedObjectDefinition src) {
        return objectMapper.convertValue(src, DiscordEmbeddedObjectDefinition.class);
    }

}
