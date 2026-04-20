package com.webhook.relay.chatterbox.adapter.out.discord.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.adapter.out.discord.model.DiscordEmbeddedObjectDefinition;
import com.webhook.relay.chatterbox.application.common.exception.ApplicationException;
import com.webhook.relay.chatterbox.application.common.template.TemplateSubstitutor;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.port.out.vendor.factory.VendorFactoryPort;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxDeliveryDiscordProperties;

import java.util.Map;

import static com.webhook.relay.chatterbox.adapter.out.github.model.GithubEventMapping.GithubIncomingMappingFieldKeys.*;

@Component("discordEmbeddedObjectFactory")
@RequiredArgsConstructor
public class DiscordEmbeddedObjectFactory implements VendorFactoryPort {

    private final ChatterboxDeliveryDiscordProperties discordProperties;

    private final TemplateSubstitutor substitutionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DiscordEmbeddedObjectDefinition buildDefinition(Map<String, String> values) {
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
    public DiscordEmbeddedObjectDefinition buildDefinition(OutboundEvent outboundEvent) {
        Map<String, String> values = Map.of(
                "displayName", outboundEvent.title(),
                REPOSITORYNAME.getFieldName(), outboundEvent.repository(),
                SENDERNAME.getFieldName(), outboundEvent.actor(),
                URL.getFieldName(), outboundEvent.url(),
                URLDISPLAYTEXT.getFieldName(), outboundEvent.displayText(),
                EXTRADETAIL.getFieldName(), outboundEvent.extra()
        );
        return buildDefinition(values);
    }

    @Override
    public String getAsPayloadString(OutboundEvent eventDto) throws ApplicationException {
        DiscordEmbeddedObjectDefinition embeddedObjectDefinition = buildDefinition(eventDto);
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
