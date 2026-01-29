package za.co.psybergate.chatterbox.application.domain.event.model;

import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;

import java.time.LocalDateTime;

public record GithubPolledEventDeliveryDto(
        Long id,
        Long githubPolledEventId,
        String deliveryDestination,
        String deliveryDestinationUrl,
        WebhookEventStatus webhookEventStatus,
        LocalDateTime deliveredAt) {

}
