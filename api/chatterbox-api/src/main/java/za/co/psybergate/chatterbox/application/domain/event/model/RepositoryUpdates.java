package za.co.psybergate.chatterbox.application.domain.event.model;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public class RepositoryUpdates {

    private final @NotNull LocalDateTime fromDate;

    private final @NotNull LocalDateTime untilDate;

    private final @NotNull Map<WebhookEventType, List<?>> webhookEventTypeDetails;

    public RepositoryUpdates(@NotNull LocalDateTime fromDate, @NotNull LocalDateTime untilDate) {
        this.fromDate = fromDate;
        this.untilDate = untilDate;
        this.webhookEventTypeDetails = new HashMap<>();
    }

    public void add(WebhookEventType webhookEventType, List<?> details) {
        webhookEventTypeDetails.put(webhookEventType, details);
    }

}
