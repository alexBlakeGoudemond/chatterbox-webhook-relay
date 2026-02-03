package za.co.psybergate.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.application.common.exception.UnrecognizedRequestException;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventMapping;
import za.co.psybergate.chatterbox.application.domain.configuration.EventPayloadMapping;
import za.co.psybergate.chatterbox.adapter.common.map.AdapterMapper;

import java.util.HashMap;
import java.util.Map;

/// [WebhookEventType] can be defined in the properties files and loaded here, as can [GithubEventMapping]
@Data
@ConfigurationProperties(prefix = "chatterbox.sources.github.payload")
public class ChatterboxSourceGithubPayloadProperties {

    private Map<String, GithubEventMapping> eventMapping;

    public void setEventMapping(Map<String, GithubEventMapping> eventMapping) {
        Map<String, GithubEventMapping> formattedEventMapping = new HashMap<>();
        for (Map.Entry<String, GithubEventMapping> entry : eventMapping.entrySet()) {
            formattedEventMapping.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        this.eventMapping = formattedEventMapping;
    }

    public boolean containsEvent(String eventType) {
        return eventMapping.containsKey(eventType.toUpperCase());
    }

    public EventPayloadMapping getEventPayloadMapping(String eventType) throws UnrecognizedRequestException {
        if (!containsEvent(eventType)) {
            throw new UnrecognizedRequestException(String.format("Unsupported event type '%s'", eventType));
        }
        GithubEventMapping githubMapping = eventMapping.get(eventType.toUpperCase());
        return AdapterMapper.mapToEventPayloadMapping(githubMapping);
    }

}
