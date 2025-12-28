package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "github_polled_event",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_type", "source_id", "repository_full_name"})
)
@Getter
@Setter
@NoArgsConstructor
public class GithubPolledEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_full_name", nullable = false)
    private String repositoryFullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "fetched_at", nullable = false, updatable = false)
    private Instant fetchedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    public GithubPolledEvent(EventType eventType, String sourceId, String repositoryFullName, String payload, EventStatus status) {
        this.eventType = eventType;
        this.sourceId = sourceId;
        this.repositoryFullName = repositoryFullName;
        this.payload = payload;
        this.status = status;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GithubPolledEvent that = (GithubPolledEvent) object;
        return eventType == that.eventType && Objects.equals(sourceId, that.sourceId) && Objects.equals(repositoryFullName, that.repositoryFullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, sourceId, repositoryFullName);
    }

}

