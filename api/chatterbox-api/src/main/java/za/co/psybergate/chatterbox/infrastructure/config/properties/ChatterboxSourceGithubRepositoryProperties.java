package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.source.github.repository")
public class ChatterboxSourceGithubRepositoryProperties {

    private List<DestinationMappings> destinationMappings;

    public boolean acceptsRepository(String repositoryName) {
        for (DestinationMappings destinationMapping : destinationMappings) {
            if (destinationMapping.getName().equalsIgnoreCase(repositoryName)) {
                return true;
            }
        }
        return false;
    }

    @Data
    public static class DestinationMappings {

        private String name;

        private String destinationChannel;

    }

}
