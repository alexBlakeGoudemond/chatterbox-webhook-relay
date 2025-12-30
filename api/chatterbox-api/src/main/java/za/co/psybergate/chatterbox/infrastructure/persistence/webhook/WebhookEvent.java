package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "webhook_event")
@NoArgsConstructor
@Getter
@Setter
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_full_name", nullable = false)
    private String repositoryFullName;

    @Column(name = "webhook_id", nullable = false, unique = true)
    private String webhookId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(columnDefinition = "delivery_destination", nullable = false)
    private String deliveryDestination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    public WebhookEvent(String webhookId, String repositoryFullName, EventType eventType, String payload, EventStatus status) {
        this.webhookId = webhookId;
        this.repositoryFullName = repositoryFullName;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
    }

    public WebhookEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        this(uniqueId, eventDto.repositoryName(), eventDto.eventType(), rawBody.toString(), EventStatus.RECEIVED);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        WebhookEvent that = (WebhookEvent) object;
        return Objects.equals(repositoryFullName, that.repositoryFullName) && Objects.equals(webhookId, that.webhookId) && eventType == that.eventType && Objects.equals(payload, that.payload) && Objects.equals(deliveryDestination, that.deliveryDestination) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryFullName, webhookId, eventType, payload, deliveryDestination, status);
    }

}
