package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "teams-card-template.adaptive-card")
public class TeamsAdaptiveCardTemplateProperties extends TeamsAdaptiveCardTemplate {

}
