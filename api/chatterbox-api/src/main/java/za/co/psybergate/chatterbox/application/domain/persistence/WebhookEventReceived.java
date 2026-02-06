package za.co.psybergate.chatterbox.application.domain.persistence;

import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;

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
