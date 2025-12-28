package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "webhook_received")
@Getter
@Setter
public class WebhookReceived {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_id", unique = true)
    private String webhookId;

    @Column(name = "repository_full_name")
    private String repositoryFullName;

    @Column(name = "event_type")
    private String eventType;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        WebhookReceived that = (WebhookReceived) object;
        return Objects.equals(webhookId, that.webhookId) && Objects.equals(repositoryFullName, that.repositoryFullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webhookId, repositoryFullName);
    }

}
