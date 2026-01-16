package za.co.psybergate.chatterbox.domain.record;

import za.co.psybergate.chatterbox.domain.api.EventStatus;

import java.time.LocalDateTime;

public record WebhookEventDeliveryRecord(
        Long id,
        Long webhookEventId,
        String deliveryDestination,
        String deliveryDestinationUrl,
        EventStatus eventStatus,
        LocalDateTime deliveredAt) {

}
