package za.co.psybergate.chatterbox.domain.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import za.co.psybergate.chatterbox.domain.api.EventStatus;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class WebhookEventDeliveryRecord {

    private Long id;

    private Long webhookEventId;

    private String deliveryDestination;

    private String deliveryDestinationUrl;

    private EventStatus eventStatus;

    private LocalDateTime deliveredAt;

}
