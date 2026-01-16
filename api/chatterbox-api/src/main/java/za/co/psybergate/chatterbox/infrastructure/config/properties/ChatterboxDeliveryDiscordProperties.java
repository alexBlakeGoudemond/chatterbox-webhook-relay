package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import za.co.psybergate.chatterbox.domain.discord.DiscordEmbeddedObjectDefinition;

/// Reference Material:
/// [Executing Discord Webhook](https://discord.com/developers/docs/resources/webhook#execute-webhook)
///
/// The JSON payload can be built up with multiple
/// [Embed Objects](https://discord.com/developers/docs/resources/message#embed-object)
///
/// Example
/// ```
/// {
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
@ConfigurationProperties(prefix = "chatterbox.deliveries.discord")
public class ChatterboxDeliveryDiscordProperties {

    private DiscordEmbeddedObjectDefinition embeddedObjectDefinition;

}
