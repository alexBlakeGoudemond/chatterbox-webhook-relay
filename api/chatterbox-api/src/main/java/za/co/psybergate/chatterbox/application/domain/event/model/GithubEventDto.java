package za.co.psybergate.chatterbox.application.domain.event.model;

import jakarta.validation.constraints.NotNull;
import za.co.psybergate.chatterbox.application.domain.api.EventType;

public record GithubEventDto(
        @NotNull EventType eventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText,
        @NotNull String extraDetail) {

}
