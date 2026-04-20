package com.webhook.relay.chatterbox.adapter.out.teams.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.adapter.out.teams.model.TeamsAdaptiveCardDefinition;
import com.webhook.relay.chatterbox.application.common.exception.ApplicationException;
import com.webhook.relay.chatterbox.application.common.template.RegexTemplateSubstitutor;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.port.out.vendor.factory.VendorFactoryPort;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxDeliveryTeamsProperties;

import java.util.Map;

import static com.webhook.relay.chatterbox.adapter.out.github.model.GithubEventMapping.GithubIncomingMappingFieldKeys.*;

@Component("teamsAdaptiveCardFactory")
@RequiredArgsConstructor
public class TeamsAdaptiveCardFactory implements VendorFactoryPort {

    private final ChatterboxDeliveryTeamsProperties teamsProperties;

    private final RegexTemplateSubstitutor substitutionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TeamsAdaptiveCardDefinition buildDefinition(Map<String, String> values) {
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
    public TeamsAdaptiveCardDefinition buildDefinition(OutboundEvent outboundEvent) {
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

    private TeamsAdaptiveCardDefinition deepCopy(TeamsAdaptiveCardDefinition src) {
        return objectMapper.convertValue(src, TeamsAdaptiveCardDefinition.class);
    }

    @Override
    public String getAsPayloadString(OutboundEvent outboundEvent) throws ApplicationException {
        TeamsAdaptiveCardDefinition teamsAdaptiveCardDefinition = buildDefinition(outboundEvent);
        String teamsPayload;
        try {
            teamsPayload = objectMapper.writeValueAsString(teamsAdaptiveCardDefinition);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Unexpected issue when converting EventDto to Json String", e);
        }
        return teamsPayload;
    }

}

