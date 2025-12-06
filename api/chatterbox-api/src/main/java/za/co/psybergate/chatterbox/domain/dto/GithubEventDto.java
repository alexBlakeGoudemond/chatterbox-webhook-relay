package za.co.psybergate.chatterbox.domain.dto;

import jakarta.validation.constraints.NotNull;

public record GithubEventDto(
        @NotNull String eventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText
) {

}
