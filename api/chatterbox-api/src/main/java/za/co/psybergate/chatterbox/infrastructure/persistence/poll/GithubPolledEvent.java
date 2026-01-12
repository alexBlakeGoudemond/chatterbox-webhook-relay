package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

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
import za.co.psybergate.chatterbox.domain.event.GithubPolledEventRecord;
import za.co.psybergate.chatterbox.infrastructure.persistence.converter.LocalDateTimeToInstantConverter;

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

    @Column(name = "fetched_at", nullable = false, updatable = false)
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime fetchedAt;

    @Column(name = "processed_at")
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime processedAt;

    public GithubPolledEvent(EventType eventType,
                             String sourceId,
                             String repositoryFullName,
                             String displayName,
                             String senderName,
                             String eventUrl,
                             String eventUrlDisplayText,
                             String extraDetail,
                             String payload,
                             EventStatus status,
                             LocalDateTime fetchedAt) {
        this.eventType = eventType;
        this.sourceId = sourceId;
        this.repositoryFullName = repositoryFullName;
        this.displayName = displayName;
        this.senderName = senderName;
        this.eventUrl = eventUrl;
        this.eventUrlDisplayText = eventUrlDisplayText;
        this.extraDetail = extraDetail;
        this.payload = payload;
        this.eventStatus = status;
        this.fetchedAt = fetchedAt;
    }

    public GithubPolledEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        this(eventDto.eventType(), uniqueId, eventDto.repositoryName(), eventDto.displayName(), eventDto.senderName(), eventDto.url(), eventDto.urlDisplayText(), eventDto.extraDetail(), rawBody.toString(), EventStatus.RECEIVED, LocalDateTime.now());
    }

    public GithubPolledEvent(GithubPolledEventRecord polledEventRecord) {
        this(polledEventRecord.getEventType(), polledEventRecord.getSourceId(), polledEventRecord.getRepositoryFullName(), polledEventRecord.getDisplayName(), polledEventRecord.getSenderName(), polledEventRecord.getEventUrl(), polledEventRecord.getEventUrlDisplayText(), polledEventRecord.getExtraDetail(), polledEventRecord.getPayload(), EventStatus.RECEIVED, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GithubPolledEvent that = (GithubPolledEvent) object;
        return Objects.equals(repositoryFullName, that.repositoryFullName) && eventType == that.eventType && Objects.equals(sourceId, that.sourceId) && Objects.equals(payload, that.payload) && eventStatus == that.eventStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryFullName, eventType, sourceId, payload, eventStatus);
    }

}

