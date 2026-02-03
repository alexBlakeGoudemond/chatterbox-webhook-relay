package za.co.psybergate.chatterbox.application.domain.event.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public class RepositoryUpdates {

    private final @NotNull LocalDateTime fromDate;

    private final @NotNull LocalDateTime untilDate;

    private final @NotNull Map<WebhookEventType, ArrayNode> webhookEventTypeDetails;

    public RepositoryUpdates(@NotNull LocalDateTime fromDate, @NotNull LocalDateTime untilDate) {
        this.fromDate = fromDate;
        this.untilDate = untilDate;
        this.webhookEventTypeDetails = new HashMap<>();
    }

    public void add(WebhookEventType webhookEventType, ArrayNode arrayNode) {
        webhookEventTypeDetails.put(webhookEventType, arrayNode);
    }

}
