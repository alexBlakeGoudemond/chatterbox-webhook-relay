package za.co.psybergate.chatterbox.application.domain.event.model;

import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;

import java.time.LocalDateTime;

// TODO BlakeGoudemond 2026/01/30 | needs to be in infra - determine what base class should be, if this is an impl
public record WebhookPolledEventDeliveryDto(
        Long id,
        Long githubPolledEventId,
        String deliveryDestination,
        String deliveryDestinationUrl,
        WebhookEventStatus webhookEventStatus,
        LocalDateTime deliveredAt) {

}
