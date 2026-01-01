package za.co.psybergate.chatterbox.domain.dto;

import jakarta.validation.constraints.NotNull;
import za.co.psybergate.chatterbox.domain.api.EventType;

public record GithubEventDto(
        @NotNull EventType eventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText) {

}
