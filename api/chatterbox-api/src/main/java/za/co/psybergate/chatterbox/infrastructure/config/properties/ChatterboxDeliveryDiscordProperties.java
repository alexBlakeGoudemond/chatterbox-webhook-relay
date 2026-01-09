package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// Reference Material:
/// [Executing Discord Webhook](https://discord.com/developers/docs/resources/webhook#execute-webhook)
///
/// The JSON payload can be built up with multiple
/// [Embed Objects](https://discord.com/developers/docs/resources/message#embed-object)
///
/// ```
/// {
///   "content": "Further Embedded Object Webhook Options 🍂👟🌲",
///   "embeds": [
///     {
///       "author": {
///         "name": "PR opened by Alex",
///         "icon_url": "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png"
///       },
///       "title": "Chatterbox API",
///       "description": "📢 Push Event for psyAlexBlakeGoudemond/chatterbox",
///       "url": "http://localhost:8080",
///       "color"6993
///    }
///   ]
/// }
/// ```
@Data
@ConfigurationProperties(prefix = "chatterbox.deliveries.discord") // TODO BlakeGoudemond 2026/01/09 | add to config
public class ChatterboxDeliveryDiscordProperties {

    private EmbeddedObjectDefinition embeddedObjectDefinition;

    // TODO BlakeGoudemond 2026/01/09 | fill out from here
    @Data
    private static class EmbeddedObjectDefinition {

    }

}
