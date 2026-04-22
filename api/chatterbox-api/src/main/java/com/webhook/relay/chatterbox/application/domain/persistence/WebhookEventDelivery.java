package com.webhook.relay.chatterbox.application.domain.persistence;

import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventStatus;

import java.time.LocalDateTime;

public record WebhookEventDelivery(
        Long id,
        Long webhookEventId,
        String deliveryDestination,
        String deliveryDestinationUrl,
        WebhookEventStatus webhookEventStatus,
        LocalDateTime deliveredAt) {

}