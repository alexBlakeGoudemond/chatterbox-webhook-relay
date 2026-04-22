package com.webhook.relay.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatterbox.security.webhook.github")
public class ChatterboxSecurityWebhookGithubProperties {

    private String secret;

}
