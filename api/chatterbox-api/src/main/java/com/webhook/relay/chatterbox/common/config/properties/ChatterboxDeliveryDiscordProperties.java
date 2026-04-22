package com.webhook.relay.chatterbox.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.webhook.relay.chatterbox.adapter.out.discord.model.DiscordEmbeddedObjectDefinition;

/**
 * Reference Material:
 * [Executing Discord Webhook](https://discord.com/developers/docs/resources/webhook#execute-webhook)
 *
 * The JSON payload can be built up with multiple
 * [Embed Objects](https://discord.com/developers/docs/resources/message#embed-object)
 *
 * Example
 * ```
 * {
 *   "embeds": [
 *     {
 *       "author": {
 *         "name": "PR opened by Alex",
 *         "icon_url": "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png"
 *       },
 *       "title": "Chatterbox API",
 *       "description": "📢 Push Event for alexBlakeGoudemond/chatterbox-webhook-relay",
 *       "url": "http://localhost:8080",
 *       "color"6993
 *    }
 *   ]
 * }
 * ```
 */
@Data
@ConfigurationProperties(prefix = "chatterbox.deliveries.discord")
public class ChatterboxDeliveryDiscordProperties {

    private DiscordEmbeddedObjectDefinition embeddedObjectDefinition;

}
