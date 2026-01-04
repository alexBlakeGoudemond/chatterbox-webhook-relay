package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.sources.github.repository")
public class ChatterboxSourceGithubRepositoryProperties {

    private List<DestinationMapping> destinationMapping;

    public boolean acceptsRepository(String repositoryName) {
        for (DestinationMapping destinationMapping : destinationMapping) {
            if (destinationMapping.getName().equalsIgnoreCase(repositoryName)) {
                return true;
            }
        }
        return false;
    }

    @Data
    public static class DestinationMapping {

        private String name;

        private String teamsDestinationChannel;

    }

}
