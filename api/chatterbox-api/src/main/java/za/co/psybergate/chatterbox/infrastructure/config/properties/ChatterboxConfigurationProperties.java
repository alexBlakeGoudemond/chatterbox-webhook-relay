package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties(prefix = "chatterbox")
public class ChatterboxConfigurationProperties {

    private List<String> githubRepositoriesAccepted;

    private List<TeamsDestination> teamsDestinationsAccepted;

    private Map<String, PayloadMapping> githubIncomingMappings;

    public boolean containsEvent(String eventType) {
        return githubIncomingMappings.containsKey(eventType);
    }

    public boolean acceptsRepository(String repositoryName) {
        repositoryName = repositoryName.replace("\"", "");
        return githubRepositoriesAccepted.contains(repositoryName);
    }

    @Data
    public static class TeamsDestination {

        private String channelName;

        private String webhookUrl;

    }

    @Data
    public static class PayloadMapping {

        private String displayName;

        private Map<String, String> fields;

    }

    @Getter
    public enum GithubIncomingMappingFields {
        REPOSITORY_NAME("repositoryName"),
        SENDER_NAME("senderName"),
        URL("url"),
        URL_DISPLAY_TEXT("urlDisplayText");

        private GithubIncomingMappingFields(String fieldName) {
            this.fieldName = fieldName;
        }

        private final String fieldName;
    }

}
