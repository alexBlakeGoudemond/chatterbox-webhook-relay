package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.converter.LocalDateTimeToInstantConverter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "webhook_event")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_full_name", nullable = false)
    private String repositoryFullName;

    @Column(name = "webhook_id", nullable = false, unique = true)
    private String webhookId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "received_at", nullable = false, updatable = false)
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime processedAt;

    public WebhookEvent(String webhookId, String repositoryFullName, EventType eventType, String payload, EventStatus status, LocalDateTime receivedAt) {
        this.webhookId = webhookId;
        this.repositoryFullName = repositoryFullName;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.receivedAt = receivedAt;
    }

    public WebhookEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        this(uniqueId, eventDto.repositoryName(), eventDto.eventType(), rawBody.toString(), EventStatus.RECEIVED, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        WebhookEvent that = (WebhookEvent) object;
        return Objects.equals(repositoryFullName, that.repositoryFullName) && Objects.equals(webhookId, that.webhookId) && eventType == that.eventType && Objects.equals(payload, that.payload) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryFullName, webhookId, eventType, payload, status);
    }

}
