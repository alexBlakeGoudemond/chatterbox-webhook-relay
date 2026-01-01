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
import za.co.psybergate.chatterbox.infrastructure.persistence.converter.LocalDateTimeToInstantConverter;

import java.time.Instant;
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

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "fetched_at", nullable = false, updatable = false)
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime fetchedAt;

    @Column(name = "processed_at")
    @Convert(converter = LocalDateTimeToInstantConverter.class)
    private LocalDateTime processedAt;

    public GithubPolledEvent(EventType eventType, String sourceId, String repositoryFullName, String payload, EventStatus status, LocalDateTime fetchedAt) {
        this.eventType = eventType;
        this.sourceId = sourceId;
        this.repositoryFullName = repositoryFullName;
        this.payload = payload;
        this.status = status;
        this.fetchedAt = fetchedAt;
    }

    public GithubPolledEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        this(eventDto.eventType(), uniqueId, eventDto.repositoryName(), rawBody.toString(), EventStatus.RECEIVED, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GithubPolledEvent that = (GithubPolledEvent) object;
        return Objects.equals(repositoryFullName, that.repositoryFullName) && eventType == that.eventType && Objects.equals(sourceId, that.sourceId) && Objects.equals(payload, that.payload) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryFullName, eventType, sourceId, payload, status);
    }

}

