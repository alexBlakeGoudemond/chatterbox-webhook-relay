package za.co.psybergate.chatterbox.adapter.out.discord.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.port.out.discord.factory.DiscordEmbeddedObjectFactoryPort;
import za.co.psybergate.chatterbox.application.common.template.TemplateSubstitutor;
import za.co.psybergate.chatterbox.adapter.out.discord.model.DiscordEmbeddedObjectDefinition;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxDeliveryDiscordProperties;

import java.util.Map;

import static za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventMapping.GithubIncomingMappingFieldKeys.*;

@Component
@RequiredArgsConstructor
public class DiscordPayloadFactory implements DiscordEmbeddedObjectFactoryPort {

    private final ChatterboxDeliveryDiscordProperties discordProperties;

    private final TemplateSubstitutor substitutionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private DiscordEmbeddedObjectDefinition deepCopy(DiscordEmbeddedObjectDefinition src) {
        return objectMapper.convertValue(src, DiscordEmbeddedObjectDefinition.class);
    }

}
