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

        private Map<GithubIncomingMappingFieldKeys, String> fields;

    }


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
