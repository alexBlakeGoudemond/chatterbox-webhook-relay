package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.infrastructure.exception.UnrecognizedRequestException;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "chatterbox.sources.github.payload")
public class ChatterboxSourceGithubPayloadProperties {

    private Map<String, EventMapping> eventMapping;

    public boolean containsEvent(String eventType) {
        return eventMapping.containsKey(eventType);
    }

    public EventMapping getEventMapping(String eventType) throws UnrecognizedRequestException {
        if (!containsEvent(eventType)) {
            throw new UnrecognizedRequestException(String.format("Unsupported event type '%s'", eventType));
        }
        return eventMapping.get(eventType);
    }

    @Data
    public static class EventMapping {

        private String displayName;

        private Map<GithubIncomingMappingFieldKeys, String> fields;

        @Getter
        public enum GithubIncomingMappingFieldKeys {
            REPOSITORYNAME("repositoryName"),
            SENDERNAME("senderName"),
            URL("url"),
            URLDISPLAYTEXT("urlDisplayText");

            GithubIncomingMappingFieldKeys(String fieldName) {
                this.fieldName = fieldName;
            }

            private final String fieldName;
        }

    }

}
