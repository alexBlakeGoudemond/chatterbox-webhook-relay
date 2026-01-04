package za.co.psybergate.chatterbox.domain.dto;

import jakarta.validation.constraints.NotNull;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;

public record GithubEventDto(
        @NotNull EventType eventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText) {

    public GithubEventDto(WebhookEvent webhookEvent) {
        this(webhookEvent.getEventType(), webhookEvent.getDisplayName(), webhookEvent.getRepositoryFullName(), webhookEvent.getSenderName(), webhookEvent.getEventUrl(), webhookEvent.getEventUrlDisplayText());
    }

    public GithubEventDto(GithubPolledEvent polledEvent) {
        this(polledEvent.getEventType(), polledEvent.getDisplayName(), polledEvent.getRepositoryFullName(), polledEvent.getSenderName(), polledEvent.getEventUrl(), polledEvent.getEventUrlDisplayText());
    }

}
