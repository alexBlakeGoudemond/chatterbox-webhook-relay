package za.co.psybergate.chatterbox.domain.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import za.co.psybergate.chatterbox.domain.api.EventStatus;

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

}
