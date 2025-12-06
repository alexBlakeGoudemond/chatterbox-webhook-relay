package za.co.psybergate.chatterbox.application.webhook.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import za.co.psybergate.chatterbox.application.webhook.validator.WebhookValidator;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties.GithubIncomingMappingFieldKeys;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

import java.util.Map;

import static za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties.GithubIncomingMappingFieldKeys.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Validated
public class GithubEventExtractor {

    private final WebhookValidator webhookValidator;

    @Valid
    public GithubEventDto extract(String eventType, JsonNode payload) throws UnrecognizedRequestException {
        var payloadMapping = webhookValidator.getPayloadMapping(eventType);
        Map<GithubIncomingMappingFieldKeys, String> fields = payloadMapping.getFields();

        return new GithubEventDto(
                eventType,
                payloadMapping.getDisplayName(),
                read(payload, fields.get(REPOSITORYNAME)),
                read(payload, fields.get(SENDERNAME)),
                read(payload, fields.get(URL)),
                read(payload, fields.get(URLDISPLAYTEXT))
        );
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
