package za.co.psybergate.chatterbox.test.helper;

import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

@Component
public class TestConfigurationResolver {

    private final WebhookConfigurationResolverPort configurationResolver;

    public TestConfigurationResolver(WebhookConfigurationResolverPort configurationResolver) {
        this.configurationResolver = configurationResolver;
    }

    public String getTeamsDestinationUrl(OutboundEvent eventDto) {
        return configurationResolver.resolveTeamsUrl(eventDto.repository());
    }

    public String getDiscordDestinationUrl(OutboundEvent outboundEvent) {
        return configurationResolver.resolveDiscordUrl(outboundEvent.repository());
    }

}
