package za.co.psybergate.chatterbox.application.webhook.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import za.co.psybergate.chatterbox.application.webhook.extractor.resolver.WebhookConfigurationResolver;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties.GithubIncomingMappingFieldKeys;

import java.util.Map;

import static za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxConfigurationProperties.GithubIncomingMappingFieldKeys.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Validated
public class GithubEventExtractorImpl implements GithubEventExtractor{

    private final WebhookConfigurationResolver webhookConfigurationResolver;

    /// Transform the eventType and JsonPayload into an internal type: [GithubEventDto].
    ///
    /// The [GithubEventDto] has simple validation setup through the constructor of the record.
    /// Thus, if Validation fails - this method will produce a [ConstraintViolationException]
    @Override
    @Valid
    public GithubEventDto extract(String eventType, JsonNode payload) throws ConstraintViolationException {
        var payloadMapping = webhookConfigurationResolver.getPayloadMapping(eventType);
        Map<GithubIncomingMappingFieldKeys, String> fields = payloadMapping.getFields();

        String repositoryName = read(payload, fields.get(REPOSITORYNAME));
        String teamsDestinationUrl = webhookConfigurationResolver.getDestinationUrl(repositoryName);
        String urlDisplayText = read(payload, fields.get(URLDISPLAYTEXT));
        String formattedUrlDisplayText = format(urlDisplayText, payloadMapping.getDisplayName());
        return new GithubEventDto(
                eventType,
                payloadMapping.getDisplayName(),
                repositoryName,
                read(payload, fields.get(SENDERNAME)),
                read(payload, fields.get(URL)),
                formattedUrlDisplayText,
                teamsDestinationUrl
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
