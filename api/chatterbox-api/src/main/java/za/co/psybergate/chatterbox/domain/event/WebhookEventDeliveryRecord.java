package za.co.psybergate.chatterbox.domain.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventDeliveryLog;

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

    public WebhookEventDeliveryRecord(WebhookEventDeliveryLog deliveryLog) {
        this.id = deliveryLog.getId();
        this.webhookEventId = deliveryLog.getWebhookEventId();
        this.deliveryDestination = deliveryLog.getDeliveryDestination();
        this.deliveryDestinationUrl = deliveryLog.getDeliveryDestinationUrl();
        this.eventStatus = deliveryLog.getEventStatus();
        this.deliveredAt = deliveryLog.getDeliveredAt();
    }

}
