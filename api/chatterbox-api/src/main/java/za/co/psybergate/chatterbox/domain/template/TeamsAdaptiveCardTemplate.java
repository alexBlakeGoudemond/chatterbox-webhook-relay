package za.co.psybergate.chatterbox.domain.template;

import lombok.Data;

import java.util.List;

@Data
public class TeamsAdaptiveCardTemplate {

    private String type = "message";

    private List<Attachment> attachments;

    @Data
    public static class Attachment {

        private String contentType = "application/vnd.microsoft.card.adaptive";

        private Content content;

    }

    @Data
    public static class Content {

        private String type = "AdaptiveCard";

        private MsTeams msteams;

        private List<BodyItem> body;

        private String $schema = "http://adaptivecards.io/schemas/adaptive-card.json";

        private String version = "1.0";

    }

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
