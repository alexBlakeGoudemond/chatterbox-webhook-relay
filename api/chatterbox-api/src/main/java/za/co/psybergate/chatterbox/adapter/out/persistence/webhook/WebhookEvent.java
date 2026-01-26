package za.co.psybergate.chatterbox.adapter.out.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import za.co.psybergate.chatterbox.application.domain.api.EventStatus;
import za.co.psybergate.chatterbox.application.domain.api.EventType;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.adapter.out.persistence.converter.LocalDateTimeToInstantConverter;

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

    @Column(name = "webhook_id", nullable = false)
    private String webhookId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "event_url", nullable = false)
    private String eventUrl;

    @Column(name = "event_url_display_text", nullable = false)
    private String eventUrlDisplayText;

    @Column(name = "extra_detail", nullable = false)
    private String extraDetail;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_status", nullable = false)
    private EventStatus eventStatus;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "received_at", nullable = false, updatable = false)
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime processedAt;

    public WebhookEvent(String webhookId,
                        String repositoryFullName,
                        EventType eventType,
                        String displayName,
                        String senderName,
                        String eventUrl,
                        String eventUrlDisplayText,
                        String extraDetail,
                        String payload,
                        EventStatus eventStatus,
                        LocalDateTime receivedAt) {
        this.webhookId = webhookId;
        this.repositoryFullName = repositoryFullName;
        this.eventType = eventType;
        this.displayName = displayName;
        this.senderName = senderName;
        this.eventUrl = eventUrl;
        this.eventUrlDisplayText = eventUrlDisplayText;
        this.extraDetail = extraDetail;
        this.payload = payload;
        this.eventStatus = eventStatus;
        this.receivedAt = receivedAt;
    }

    public WebhookEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        this(uniqueId, eventDto.repositoryName(), eventDto.eventType(), eventDto.displayName(), eventDto.senderName(), eventDto.url(), eventDto.urlDisplayText(), eventDto.extraDetail(), rawBody.toString(), EventStatus.RECEIVED, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        WebhookEvent that = (WebhookEvent) object;
        return Objects.equals(repositoryFullName, that.repositoryFullName) && Objects.equals(webhookId, that.webhookId) && eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryFullName, webhookId, eventType);
    }

}
