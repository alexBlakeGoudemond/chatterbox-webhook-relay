package za.co.psybergate.chatterbox.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

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
// TODO BlakeGoudemond 2026/01/09 | test, then bring into main properties file
public class ChatterboxDeliveryDiscordProperties {

    private EmbeddedObjectDefinition embeddedObjectDefinition;

    @Data
    private static class EmbeddedObjectDefinition {


        private List<EmbeddedObject> embeddedObjects;

        @Data
        public static class EmbeddedObject {

            private String title;

            private String description;

            private String url;

            private Integer color = 6993;

            private Author author;

            @Data
            public static class Author {

                private String name;

                private String iconUrl = "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png";

            }
        }
    }
}
