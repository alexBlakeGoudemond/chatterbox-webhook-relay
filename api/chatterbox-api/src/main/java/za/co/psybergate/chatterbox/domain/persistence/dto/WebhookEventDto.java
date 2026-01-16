package za.co.psybergate.chatterbox.domain.persistence.dto;

import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.time.LocalDateTime;

public record WebhookEventDto(
        Long id,
        String repositoryFullName,
        String webhookId,
        EventType eventType,
        String displayName,
        String senderName,
        String eventUrl,
        String eventUrlDisplayText,
        String extraDetail,
        String payload,
        EventStatus eventStatus,
        String errorMessage,
        LocalDateTime receivedAt,
        LocalDateTime processedAt) {

    // TODO BlakeGoudemond 2026/01/16 | can I remove this?
    public WebhookEventDto() {
        this(null, null, null, EventType.PUSH, null, null, null, null, null, null, EventStatus.RECEIVED, null, LocalDateTime.now(), LocalDateTime.now());
    }

}
