package za.co.psybergate.chatterbox.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.domain.template.TeamsAdaptiveCardTemplate;

@ConfigurationProperties(prefix = "chatterbox.delivery.teams.templates.card.adaptive")
public class ChatterboxDeliveryTeamsTemplateCardAdaptiveProperties extends TeamsAdaptiveCardTemplate {

}
