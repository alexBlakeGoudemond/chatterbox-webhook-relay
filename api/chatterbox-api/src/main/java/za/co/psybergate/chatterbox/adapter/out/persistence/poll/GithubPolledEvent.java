package za.co.psybergate.chatterbox.adapter.out.persistence.poll;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import za.co.psybergate.chatterbox.adapter.out.github.model.GithubEventDto;
import za.co.psybergate.chatterbox.adapter.out.persistence.converter.LocalDateTimeToInstantConverter;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "github_polled_event")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class GithubPolledEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_full_name", nullable = false)
    private String repositoryFullName;

    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false)
    private WebhookEventType webhookEventType;

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
    private WebhookEventStatus webhookEventStatus;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "fetched_at", nullable = false, updatable = false)
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime fetchedAt;

    @Column(name = "processed_at")
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime processedAt;

    public GithubPolledEvent(String sourceId,
                             String repositoryFullName,
                             WebhookEventType webhookEventType,
                             String displayName,
                             String senderName,
                             String eventUrl,
                             String eventUrlDisplayText,
                             String extraDetail,
                             String payload,
                             WebhookEventStatus status,
                             LocalDateTime fetchedAt) {
        this.webhookEventType = webhookEventType;
        this.sourceId = sourceId;
        this.repositoryFullName = repositoryFullName;
        this.displayName = displayName;
        this.senderName = senderName;
        this.eventUrl = eventUrl;
        this.eventUrlDisplayText = eventUrlDisplayText;
        this.extraDetail = extraDetail;
        this.payload = payload;
        this.webhookEventStatus = status;
        this.fetchedAt = fetchedAt;
    }

    public GithubPolledEvent(String sourceId,
                             String repositoryFullName,
                             String webhookEventType,
                             String displayName,
                             String senderName,
                             String eventUrl,
                             String eventUrlDisplayText,
                             String extraDetail,
                             String payload,
                             WebhookEventStatus status,
                             LocalDateTime fetchedAt) {
        this(sourceId,
                repositoryFullName,
                WebhookEventType.valueOf(webhookEventType),
                displayName,
                senderName,
                eventUrl,
                eventUrlDisplayText,
                extraDetail,
                payload,
                status,
                fetchedAt);
    }

    public GithubPolledEvent(String uniqueId,
                             GithubEventDto eventDto,
                             JsonNode rawBody) {
        this(uniqueId,
                eventDto.repositoryName(),
                eventDto.webhookEventType(),
                eventDto.displayName(),
                eventDto.senderName(),
                eventDto.url(),
                eventDto.urlDisplayText(),
                eventDto.extraDetail(),
                rawBody.toString(),
                WebhookEventStatus.RECEIVED,
                LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GithubPolledEvent that = (GithubPolledEvent) object;
        return Objects.equals(repositoryFullName, that.repositoryFullName) && webhookEventType == that.webhookEventType && Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryFullName, webhookEventType, sourceId);
    }

}

