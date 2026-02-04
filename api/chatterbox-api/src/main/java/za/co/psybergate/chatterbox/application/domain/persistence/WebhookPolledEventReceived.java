package za.co.psybergate.chatterbox.application.domain.persistence;

import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;

import java.time.LocalDateTime;

public record WebhookPolledEventReceived(
        Long id,
        String repositoryFullName,
        String sourceId,
        WebhookEventType webhookEventType,
        String displayName,
        String senderName,
        String eventUrl,
        String eventUrlDisplayText,
        String extraDetail,
        String payload,
        WebhookEventStatus webhookEventStatus,
        String errorMessage,
        LocalDateTime fetchedAt,
        LocalDateTime processedAt) {

}
