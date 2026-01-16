package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.record.WebhookEventRecord;
import za.co.psybergate.chatterbox.infrastructure.persistence.converter.LocalDateTimeToInstantConverter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "webhook_event_delivery_log")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class WebhookEventDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_event_id")
    private Long webhookEventId;

    @Column(name = "delivery_destination")
    private String deliveryDestination;

    @Column(name = "delivery_destination_url", columnDefinition = "text")
    private String deliveryDestinationUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_status", nullable = false)
    private EventStatus eventStatus;

    @Column(name = "delivered_at")
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime deliveredAt;

    public WebhookEventDeliveryLog(Long webhookEventId, String deliveryDestination, String deliveryDestinationUrl, EventStatus eventStatus, LocalDateTime deliveredAt) {
        this.webhookEventId = webhookEventId;
        this.deliveryDestination = deliveryDestination;
        this.deliveryDestinationUrl = deliveryDestinationUrl;
        this.eventStatus = eventStatus;
        this.deliveredAt = deliveredAt;
    }

    public WebhookEventDeliveryLog(WebhookEventRecord webhookEventRecord, String destinationName, String destinationUrl, EventStatus eventStatus) {
        this(webhookEventRecord.getId(), destinationName, destinationUrl, eventStatus, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        WebhookEventDeliveryLog that = (WebhookEventDeliveryLog) object;
        return Objects.equals(webhookEventId, that.webhookEventId) && Objects.equals(deliveryDestination, that.deliveryDestination) && Objects.equals(deliveryDestinationUrl, that.deliveryDestinationUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webhookEventId, deliveryDestination, deliveryDestinationUrl);
    }

}
