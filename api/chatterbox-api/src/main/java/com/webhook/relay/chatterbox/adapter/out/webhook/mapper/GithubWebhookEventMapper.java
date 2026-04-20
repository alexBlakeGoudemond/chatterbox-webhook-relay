package com.webhook.relay.chatterbox.adapter.out.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import com.webhook.relay.chatterbox.application.domain.configuration.EventPayloadMapping.IncomingMappingFieldKeys;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import com.webhook.relay.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

import java.util.Map;

import static com.webhook.relay.chatterbox.application.domain.configuration.EventPayloadMapping.IncomingMappingFieldKeys.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Validated
public class GithubWebhookEventMapper implements OutboundEventMapperPort {

    private final WebhookConfigurationResolverPort webhookConfigurationResolverPort;

    @Override
    public OutboundEvent map(String eventType, RawEventPayload payload) {
        return map(WebhookEventType.get(eventType), payload);
    }

    /// Transform the eventType and RawEventPayload into an internal type: [OutboundEvent].
    ///
    /// The [OutboundEvent] has simple validation setup through the constructor of the record.
    /// Thus, if Validation fails - this method will produce a [ConstraintViolationException]
    @Override
    @Valid
    public OutboundEvent map(WebhookEventType webhookEventType, RawEventPayload payload) {
        JsonNode jsonPayload = payload.getAs(JsonNode.class);
        var payloadMapping = webhookConfigurationResolverPort.getPayloadMapping(webhookEventType);
        Map<IncomingMappingFieldKeys, String> fields = payloadMapping.getFields();

        String repositoryName = read(jsonPayload, fields.get(REPOSITORYNAME));
        String urlDisplayText = read(jsonPayload, fields.get(URLDISPLAYTEXT));
        String displayName = payloadMapping.getDisplayName();
        String formattedUrlDisplayText = format(urlDisplayText, displayName);
        String senderName = read(jsonPayload, fields.get(SENDERNAME));
        String url = read(jsonPayload, fields.get(URL));
        String extraDetail = read(jsonPayload, fields.get(EXTRADETAIL));
        return new OutboundEvent(
                null,
                null,
                webhookEventType.name(),
                displayName,
                repositoryName,
                senderName,
                url,
                formattedUrlDisplayText,
                extraDetail,
                jsonPayload.toString()
        );
    }

    private String format(String urlDisplayText, String displayName) {
        if (urlDisplayText == null) {
            return displayName;
        }
        urlDisplayText = urlDisplayText.replace("\n\n", " ");
        urlDisplayText = urlDisplayText.replace("\r\n", " ");
        int maxLength = 50;
        if (urlDisplayText.length() > maxLength) {
            urlDisplayText = urlDisplayText.substring(0, maxLength) + " ...";
        }
        return urlDisplayText;
    }

    private String read(JsonNode json, String dotPath) {
        if (dotPath == null) {
            return null;
        }
        String[] keys = dotPath.split("\\.");
        JsonNode current = json;
        for (String key : keys) {
            current = current.path(key);
            if (current.isMissingNode()) return null;
        }
        return current.isValueNode() ? current.asText() : current.toString();
    }

}
