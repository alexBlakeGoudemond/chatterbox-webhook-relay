package za.co.psybergate.chatterbox.application.domain.event.model;

import jakarta.validation.constraints.NotNull;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;

// TODO BlakeGoudemond 2026/01/30 | needs to be in infra - determine what base class should be, if this is an impl
public record GithubEventDto(
        @NotNull WebhookEventType webhookEventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText,
        @NotNull String extraDetail) {

}
