package za.co.psybergate.chatterbox.domain.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventDeliveryLog;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class GithubPolledEventDeliveryRecord {

    private Long id;

    private Long githubPolledEventId;

    private String deliveryDestination;

    private String deliveryDestinationUrl;

    private EventStatus eventStatus;

    private LocalDateTime deliveredAt;

    public GithubPolledEventDeliveryRecord(GithubPolledEventDeliveryLog deliveryLog) {
        this.id = deliveryLog.getId();
        this.githubPolledEventId = deliveryLog.getGithubPolledEventId();
        this.deliveryDestination = deliveryLog.getDeliveryDestination();
        this.deliveryDestinationUrl = deliveryLog.getDeliveryDestinationUrl();
        this.eventStatus = deliveryLog.getEventStatus();
        this.deliveredAt = deliveryLog.getDeliveredAt();
    }

}
