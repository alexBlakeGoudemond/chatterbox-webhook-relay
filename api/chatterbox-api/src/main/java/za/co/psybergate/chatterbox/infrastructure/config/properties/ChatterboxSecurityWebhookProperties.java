package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatterbox.security.webhook")
public class ChatterboxSecurityWebhookProperties {

    private SecurityDetail details;

    @Data
    public static class SecurityDetail {

        private String secret;

    }

}
