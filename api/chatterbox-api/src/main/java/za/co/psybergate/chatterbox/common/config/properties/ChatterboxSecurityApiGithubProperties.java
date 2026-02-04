package za.co.psybergate.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatterbox.security.api.github")
public class ChatterboxSecurityApiGithubProperties {

    private String token;

}
