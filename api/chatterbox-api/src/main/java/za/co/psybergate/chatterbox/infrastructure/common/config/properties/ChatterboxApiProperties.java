package za.co.psybergate.chatterbox.infrastructure.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatterbox.api")
public class ChatterboxApiProperties {

    private String prefix;

}
