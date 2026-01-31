package za.co.psybergate.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubDestinationMapping;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.sources.github.repository")
public class ChatterboxSourceGithubRepositoryProperties {

    private List<GithubDestinationMapping> destinationMapping;

    public boolean acceptsRepository(String repositoryName) {
        for (GithubDestinationMapping destinationMapping : destinationMapping) {
            if (destinationMapping.getName().equalsIgnoreCase(repositoryName)) {
                return true;
            }
        }
        return false;
    }

}
