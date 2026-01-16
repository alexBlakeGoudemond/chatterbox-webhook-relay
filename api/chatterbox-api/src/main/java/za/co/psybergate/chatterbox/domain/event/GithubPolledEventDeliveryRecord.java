package za.co.psybergate.chatterbox.domain.event;

import lombok.Data;
import lombok.ToString;
import za.co.psybergate.chatterbox.domain.api.EventStatus;

import java.time.LocalDateTime;

@Data
@ToString
public class GithubPolledEventDeliveryRecord {

    private Long id;

    private Long githubPolledEventId;

    private String deliveryDestination;

    private String deliveryDestinationUrl;

    private EventStatus eventStatus;

    private LocalDateTime deliveredAt;

    public GithubPolledEventDeliveryRecord(Long id, Long githubPolledEventId, String deliveryDestination, String deliveryDestinationUrl, EventStatus eventStatus, LocalDateTime deliveredAt) {
        this.id = id;
        this.githubPolledEventId = githubPolledEventId;
        this.deliveryDestination = deliveryDestination;
        this.deliveryDestinationUrl = deliveryDestinationUrl;
        this.eventStatus = eventStatus;
        this.deliveredAt = deliveredAt;
    }

}
