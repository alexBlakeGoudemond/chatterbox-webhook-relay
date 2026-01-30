package za.co.psybergate.chatterbox.application.domain.discord.model;

import lombok.Data;

import java.util.List;

// TODO BlakeGoudemond 2026/01/27 | should be in infra - if no longer support this, it would be deleted
@Data
public class DiscordEmbeddedObjectDefinition {

    private List<EmbeddedObject> embeds;

    @Data
    public static class EmbeddedObject {

        private String title;

        private String description;

        private String url;

        private Integer color = 6993;

        private EmbeddedObject.Author author;

        @Data
        public static class Author {

            private String name;

            private String icon_url = "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png";

        }

    }

}
