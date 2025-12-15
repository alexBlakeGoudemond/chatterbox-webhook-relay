package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatterbox.api")
public class ChatterboxApiProperties {

    private ApiDetail details;

    @Data
    public static class ApiDetail {

        private String prefix;

    }

}
