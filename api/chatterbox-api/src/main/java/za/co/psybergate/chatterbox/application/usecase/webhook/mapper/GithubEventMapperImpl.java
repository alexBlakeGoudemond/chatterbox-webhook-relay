package za.co.psybergate.chatterbox.application.usecase.webhook.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.github.model.GithubEventMapping.GithubIncomingMappingFieldKeys;

import java.util.Map;

import static za.co.psybergate.chatterbox.domain.github.model.GithubEventMapping.GithubIncomingMappingFieldKeys.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Validated
public class GithubEventMapperImpl implements GithubEventMapper {

    private final WebhookConfigurationResolverPort webhookConfigurationResolverPort;

    @Override
    public GithubEventDto map(String eventType, JsonNode payload) {
        return map(EventType.get(eventType), payload);
    }

    /// Transform the eventType and JsonPayload into an internal type: [GithubEventDto].
    ///
    /// The [GithubEventDto] has simple validation setup through the constructor of the record.
    /// Thus, if Validation fails - this method will produce a [ConstraintViolationException]
    @Override
    @Valid
    public GithubEventDto map(EventType eventType, JsonNode payload) {
        var payloadMapping = webhookConfigurationResolverPort.getPayloadMapping(eventType);
        Map<GithubIncomingMappingFieldKeys, String> fields = payloadMapping.getFields();

        String repositoryName = read(payload, fields.get(REPOSITORYNAME));
        String urlDisplayText = read(payload, fields.get(URLDISPLAYTEXT));
        String displayName = payloadMapping.getDisplayName();
        String formattedUrlDisplayText = format(urlDisplayText, displayName);
        String senderName = read(payload, fields.get(SENDERNAME));
        String url = read(payload, fields.get(URL));
        String extraDetail = read(payload, fields.get(EXTRADETAIL));
        return new GithubEventDto(
                eventType,
                displayName,
                repositoryName,
                senderName,
                url,
                formattedUrlDisplayText,
                extraDetail
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
