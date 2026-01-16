package za.co.psybergate.chatterbox.domain.persistence.dto;

import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.time.LocalDateTime;

public record WebhookEventRecord(
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

}
