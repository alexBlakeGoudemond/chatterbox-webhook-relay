package za.co.psybergate.chatterbox.application.domain.persistence;

import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;

import java.time.LocalDateTime;

public record WebhookPolledEventDelivery(
        Long id,
        Long githubPolledEventId,
        String deliveryDestination,
        String deliveryDestinationUrl,
        WebhookEventStatus webhookEventStatus,
        LocalDateTime deliveredAt) {

}