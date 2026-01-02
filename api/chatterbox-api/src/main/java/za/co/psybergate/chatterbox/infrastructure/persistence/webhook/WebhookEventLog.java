package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import za.co.psybergate.chatterbox.infrastructure.persistence.converter.LocalDateTimeToInstantConverter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "webhook_event_log")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class WebhookEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_event_id")
    private Long webhookEventId;

    @Column(name = "delivery_destination")
    private String deliveryDestination;

    @Column(name = "delivery_destination_url")
    private String deliveryDestinationUrl;

    @Column(name = "delivered_at")
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime delivered_at;

    public WebhookEventLog(Long webhookEventId, String deliveryDestination, String deliveryDestinationUrl, LocalDateTime delivered_at) {
        this.webhookEventId = webhookEventId;
        this.deliveryDestination = deliveryDestination;
        this.deliveryDestinationUrl = deliveryDestinationUrl;
        this.delivered_at = delivered_at;
    }

    public WebhookEventLog(WebhookEvent webhookEvent, String destinationName, String destinationUrl) {
        this(webhookEvent.getId(), destinationName, destinationUrl, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        WebhookEventLog that = (WebhookEventLog) object;
        return Objects.equals(webhookEventId, that.webhookEventId) && Objects.equals(deliveryDestination, that.deliveryDestination) && Objects.equals(deliveryDestinationUrl, that.deliveryDestinationUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webhookEventId, deliveryDestination, deliveryDestinationUrl);
    }

}
