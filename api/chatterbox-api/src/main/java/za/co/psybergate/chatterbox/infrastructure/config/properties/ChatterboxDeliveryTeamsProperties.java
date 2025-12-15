package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;

@Data
@ConfigurationProperties(prefix = "chatterbox.delivery.teams")
public class ChatterboxDeliveryTeamsProperties {

    private TeamsAdaptiveCardTemplate adaptiveCardTemplate;


}
