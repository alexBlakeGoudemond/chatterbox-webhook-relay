package za.co.psybergate.chatterbox.application.persistence.dto;

import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.time.LocalDateTime;

public record GithubPolledEventDto(
        Long id,
        String repositoryFullName,
        String sourceId,
        EventType eventType,
        String displayName,
        String senderName,
        String eventUrl,
        String eventUrlDisplayText,
        String extraDetail,
        String payload,
        EventStatus eventStatus,
        String errorMessage,
        LocalDateTime fetchedAt,
        LocalDateTime processedAt) {

}
