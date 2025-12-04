package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
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

}
