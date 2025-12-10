package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

import java.util.List;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties(prefix = "chatterbox")
public class ChatterboxConfigurationProperties {

    private List<AcceptedRepository> githubRepositoriesAccepted;

    private List<TeamsDestination> teamsDestinationsAccepted;

    private Map<String, PayloadMapping> githubIncomingMappings;

    public boolean containsEvent(String eventType) {
        return githubIncomingMappings.containsKey(eventType);
    }

    public boolean acceptsRepository(String repositoryName) {
        for (AcceptedRepository acceptedRepository : githubRepositoriesAccepted) {
            if (acceptedRepository.getName().equals(repositoryName)) {
                return true;
            }
        }
        return false;
//        repositoryName = repositoryName.replace("\"", "");
//        return githubRepositoriesAccepted.contains(repositoryName);
    }

    public String getTeamsDestinationUrl(String destinationChannel) {
        for (TeamsDestination teamsDestination : teamsDestinationsAccepted) {
            if (teamsDestination.getChannelName().equals(destinationChannel)) {
                return teamsDestination.getWebhookUrl();
            }
        }
        throw new InternalServerException("Unable to find the teams destination channel " + destinationChannel);
    }

    @Data
    public static class TeamsDestination {

        private String channelName;

        private String webhookUrl;

    }

    @Data
    public static class AcceptedRepository {

        private String name;

        private String destinationChannel;

    }

    @Data
    public static class PayloadMapping {

        private String displayName;

        private Map<GithubIncomingMappingFieldKeys, String> fields;

        private List<String> destinations;

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
