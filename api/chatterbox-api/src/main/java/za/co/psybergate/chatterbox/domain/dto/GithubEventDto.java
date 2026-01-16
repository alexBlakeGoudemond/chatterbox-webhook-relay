package za.co.psybergate.chatterbox.domain.dto;

import jakarta.validation.constraints.NotNull;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.persistence.dto.GithubPolledEventRecord;
import za.co.psybergate.chatterbox.domain.persistence.dto.WebhookEventRecord;

public record GithubEventDto(
        @NotNull EventType eventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText,
        @NotNull String extraDetail) {

    public GithubEventDto(WebhookEventRecord webhookEventRecord) {
        this(webhookEventRecord.eventType(), webhookEventRecord.displayName(), webhookEventRecord.repositoryFullName(), webhookEventRecord.senderName(), webhookEventRecord.eventUrl(), webhookEventRecord.eventUrlDisplayText(), webhookEventRecord.extraDetail());
    }

    public GithubEventDto(GithubPolledEventRecord polledEventRecord) {
        this(polledEventRecord.eventType(), polledEventRecord.displayName(), polledEventRecord.repositoryFullName(), polledEventRecord.senderName(), polledEventRecord.eventUrl(), polledEventRecord.eventUrlDisplayText(), polledEventRecord.extraDetail());
    }

}
