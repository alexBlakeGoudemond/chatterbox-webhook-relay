package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "chatterbox.source.github.payload")
public class ChatterboxSourceGithubPayloadProperties {

    private Map<String, EventMapping> eventMappings;

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
