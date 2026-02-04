package za.co.psybergate.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.application.domain.configuration.DestinationMapping;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.sources.github.repository")
public class ChatterboxSourceGithubRepositoryProperties {

    private List<DestinationMapping> destinationMapping;

    public boolean acceptsRepository(String repositoryName) {
        for (DestinationMapping destinationMapping : destinationMapping) {
            if (destinationMapping.source().equalsIgnoreCase(repositoryName)) {
                return true;
            }
        }
        return false;
    }

}
