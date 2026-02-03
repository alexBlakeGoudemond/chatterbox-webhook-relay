package za.co.psybergate.chatterbox.adapter.out.github.model;

import jakarta.validation.constraints.NotNull;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;

public record GithubEventDto(
        @NotNull WebhookEventType webhookEventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText,
        @NotNull String extraDetail) {

}
