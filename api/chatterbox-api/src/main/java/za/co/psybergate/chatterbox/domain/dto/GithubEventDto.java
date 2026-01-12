package za.co.psybergate.chatterbox.domain.dto;

import jakarta.validation.constraints.NotNull;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.event.GithubPolledEventRecord;
import za.co.psybergate.chatterbox.domain.event.WebhookEventRecord;

public record GithubEventDto(
        @NotNull EventType eventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText,
        @NotNull String extraDetail) {

    public GithubEventDto(WebhookEventRecord webhookEventRecord) {
        this(webhookEventRecord.getEventType(), webhookEventRecord.getDisplayName(), webhookEventRecord.getRepositoryFullName(), webhookEventRecord.getSenderName(), webhookEventRecord.getEventUrl(), webhookEventRecord.getEventUrlDisplayText(), webhookEventRecord.getExtraDetail());
    }

    public GithubEventDto(GithubPolledEventRecord polledEventRecord) {
        this(polledEventRecord.getEventType(), polledEventRecord.getDisplayName(), polledEventRecord.getRepositoryFullName(), polledEventRecord.getSenderName(), polledEventRecord.getEventUrl(), polledEventRecord.getEventUrlDisplayText(), polledEventRecord.getExtraDetail());
    }

}
