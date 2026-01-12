package za.co.psybergate.chatterbox.application.webhook.processing;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import za.co.psybergate.chatterbox.application.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.webhook.routing.WebhookConfigurationResolver;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties.EventMapping.GithubIncomingMappingFieldKeys;

import java.util.Map;

import static za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties.EventMapping.GithubIncomingMappingFieldKeys.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Validated
public class GithubEventExtractorImpl implements GithubEventExtractor{

    private final WebhookConfigurationResolver webhookConfigurationResolver;

    @Override
    public GithubEventDto extract(String eventType, JsonNode payload) throws ConstraintViolationException, UnrecognizedRequestException {
        return extract(EventType.get(eventType), payload);
    }

    /// Transform the eventType and JsonPayload into an internal type: [GithubEventDto].
    ///
    /// The [GithubEventDto] has simple validation setup through the constructor of the record.
    /// Thus, if Validation fails - this method will produce a [ConstraintViolationException]
    @Override
    @Valid
    public GithubEventDto extract(EventType eventType, JsonNode payload) throws ConstraintViolationException, UnrecognizedRequestException {
        var payloadMapping = webhookConfigurationResolver.getPayloadMapping(eventType);
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
