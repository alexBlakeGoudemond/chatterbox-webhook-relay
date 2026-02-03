package za.co.psybergate.chatterbox.application.domain.event.model;

import java.time.LocalDateTime;

public record WebhookEventReceived(
        Long id,
        String repositoryFullName,
        String webhookId,
        WebhookEventType webhookEventType,
        String displayName,
        String senderName,
        String eventUrl,
        String eventUrlDisplayText,
        String extraDetail,
        String payload,
        WebhookEventStatus webhookEventStatus,
        String errorMessage,
        LocalDateTime receivedAt,
        LocalDateTime processedAt) {

}
