package za.co.psybergate.chatterbox.domain.dto;

import jakarta.validation.constraints.NotNull;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.persistence.dto.GithubPolledEventDto;
import za.co.psybergate.chatterbox.domain.persistence.dto.WebhookEventDto;

public record GithubEventDto(
        @NotNull EventType eventType,
        @NotNull String displayName,
        @NotNull String repositoryName,
        @NotNull String senderName,
        @NotNull String url,
        @NotNull String urlDisplayText,
        @NotNull String extraDetail) {

    public GithubEventDto(WebhookEventDto webhookEventDto) {
        this(webhookEventDto.eventType(), webhookEventDto.displayName(), webhookEventDto.repositoryFullName(), webhookEventDto.senderName(), webhookEventDto.eventUrl(), webhookEventDto.eventUrlDisplayText(), webhookEventDto.extraDetail());
    }

    public GithubEventDto(GithubPolledEventDto polledEventRecord) {
        this(polledEventRecord.eventType(), polledEventRecord.displayName(), polledEventRecord.repositoryFullName(), polledEventRecord.senderName(), polledEventRecord.eventUrl(), polledEventRecord.eventUrlDisplayText(), polledEventRecord.extraDetail());
    }

}
