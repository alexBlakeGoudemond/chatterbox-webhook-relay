package za.co.psybergate.chatterbox.application.domain.event.model;

import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;

import java.time.LocalDateTime;

public record WebhookPolledEventReceivedDto(
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
