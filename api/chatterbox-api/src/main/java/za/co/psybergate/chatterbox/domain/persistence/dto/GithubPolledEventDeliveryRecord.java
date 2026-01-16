package za.co.psybergate.chatterbox.domain.persistence.dto;

import za.co.psybergate.chatterbox.domain.api.EventStatus;

import java.time.LocalDateTime;

public record GithubPolledEventDeliveryRecord(
        Long id,
        Long githubPolledEventId,
        String deliveryDestination,
        String deliveryDestinationUrl,
        EventStatus eventStatus,
        LocalDateTime deliveredAt) {

}
