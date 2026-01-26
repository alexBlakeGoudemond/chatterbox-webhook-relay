package za.co.psybergate.chatterbox.application.domain.event.model;

import za.co.psybergate.chatterbox.application.domain.api.EventStatus;
import za.co.psybergate.chatterbox.application.domain.api.EventType;

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
