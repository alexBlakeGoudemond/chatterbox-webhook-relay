package za.co.psybergate.chatterbox.domain.record;

import lombok.Data;
import lombok.ToString;
import za.co.psybergate.chatterbox.domain.api.EventStatus;

import java.time.LocalDateTime;

// TODO BlakeGoudemond 2026/01/16 | convert to record
@Data
@ToString
public class WebhookEventDeliveryRecord {

    private Long id;

    private Long webhookEventId;

    private String deliveryDestination;

    private String deliveryDestinationUrl;

    private EventStatus eventStatus;

    private LocalDateTime deliveredAt;

    public WebhookEventDeliveryRecord(Long id, Long webhookEventId, String deliveryDestination, String deliveryDestinationUrl, EventStatus eventStatus, LocalDateTime deliveredAt) {
        this.id = id;
        this.webhookEventId = webhookEventId;
        this.deliveryDestination = deliveryDestination;
        this.deliveryDestinationUrl = deliveryDestinationUrl;
        this.eventStatus = eventStatus;
        this.deliveredAt = deliveredAt;
    }

}
