package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.infrastructure.persistence.converter.LocalDateTimeToInstantConverter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "github_polled_event_delivery_log")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GithubPolledEventDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_polled_event_id")
    private Long githubPolledEventId;

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
    private LocalDateTime delivered_at;

    public GithubPolledEventDeliveryLog(Long githubPolledEventId, String deliveryDestination, String deliveryDestinationUrl, LocalDateTime delivered_at) {
        this.githubPolledEventId = githubPolledEventId;
        this.deliveryDestination = deliveryDestination;
        this.deliveryDestinationUrl = deliveryDestinationUrl;
        this.delivered_at = delivered_at;
    }

    public GithubPolledEventDeliveryLog(GithubPolledEvent polledEvent, String exampleDestination, String exampleDestinationUrl) {
        this(polledEvent.getId(), exampleDestination, exampleDestinationUrl, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GithubPolledEventDeliveryLog that = (GithubPolledEventDeliveryLog) object;
        return Objects.equals(githubPolledEventId, that.githubPolledEventId) && Objects.equals(deliveryDestination, that.deliveryDestination) && Objects.equals(deliveryDestinationUrl, that.deliveryDestinationUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(githubPolledEventId, deliveryDestination, deliveryDestinationUrl);
    }

}
