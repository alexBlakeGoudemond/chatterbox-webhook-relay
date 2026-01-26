package za.co.psybergate.chatterbox.application.domain.event.model;

import za.co.psybergate.chatterbox.application.domain.api.EventStatus;

import java.time.LocalDateTime;

public record GithubPolledEventDeliveryDto(
        Long id,
        Long githubPolledEventId,
        String deliveryDestination,
        String deliveryDestinationUrl,
        EventStatus eventStatus,
        LocalDateTime deliveredAt) {

}
