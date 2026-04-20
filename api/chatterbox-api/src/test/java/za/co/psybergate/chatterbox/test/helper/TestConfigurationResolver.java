package com.webhook.relay.chatterbox.test.helper;

import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.domain.delivery.DeliveryChannelType;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

@Component
public class TestConfigurationResolver {

    private final WebhookConfigurationResolverPort configurationResolver;

    public TestConfigurationResolver(WebhookConfigurationResolverPort configurationResolver) {
        this.configurationResolver = configurationResolver;
    }

    public String getTeamsDestinationUrl(OutboundEvent outboundEvent) {
        return configurationResolver.resolveDestinationUrl(outboundEvent.repository(), DeliveryChannelType.NOTIFICATION);
    }

    public String getDiscordDestinationUrl(OutboundEvent outboundEvent) {
        return configurationResolver.resolveDestinationUrl(outboundEvent.repository(), DeliveryChannelType.CHAT);
    }

}
