package za.co.psybergate.chatterbox.infrastructure.config.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "chatterbox.delivery.teams.templates")
public class ChatterboxDeliveryTeamsTemplateProperties {

    private Map<String, CardType> cards;

    @Data
    public static class CardType {

        private String type = "message";

        private List<Attachment> attachments;

        @Data
        public static class Attachment {

            private String contentType = "application/vnd.microsoft.card.adaptive";

            private Content content;

            @Data
            public static class Content {

                private String type = "AdaptiveCard";

                private MsTeams msteams;

                private List<BodyItem> body;

                @JsonProperty("$schema")
                private String schema = "http://adaptivecards.io/schemas/adaptive-card.json";

                private String version = "1.0";

                @Data
                public static class MsTeams {

                    private String width = "Full";

                }

                @Data
                public static class BodyItem {

                    private String type = "TextBlock";

                    private String text;

                    private boolean wrap = true;

                }

            }

        }

    }

}
