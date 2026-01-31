package za.co.psybergate.chatterbox.adapter.out.github.model;

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
public class GithubRepositoryInformationDto {

    private final @NotNull LocalDateTime fromDate;

    private final @NotNull LocalDateTime untilDate;

    private final @NotNull Map<WebhookEventType, ArrayNode> githubEventTypeDetails;

    public GithubRepositoryInformationDto(
            @NotNull LocalDateTime fromDate,
            @NotNull LocalDateTime untilDate
    ) {
        this.fromDate = fromDate;
        this.untilDate = untilDate;
        this.githubEventTypeDetails = new HashMap<>();
    }

    public void add(WebhookEventType webhookEventType, ArrayNode arrayNode) {
        githubEventTypeDetails.put(webhookEventType, arrayNode);
    }

}
