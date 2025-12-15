package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "chatterbox.source.github.repository")
public class ChatterboxSourceGithubRepositoryProperties {

    private List<DestinationMappings> destinationMappings;

    @Data
    public static class DestinationMappings {

        private String name;

        private String destinationChannel;

    }

}
