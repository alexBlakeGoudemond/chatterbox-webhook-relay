package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "github_polled_event_log")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GithubPolledEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_polled_event_id")
    private Long githubPolledEventId;

    @Column(name = "delivery_destination")
    private String deliveryDestination;

    @Column(name = "delivery_destination_url")
    private String deliveryDestinationUrl;

    @Column(name = "delivered_at")
    private Instant delivered_at;

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GithubPolledEventLog that = (GithubPolledEventLog) object;
        return Objects.equals(githubPolledEventId, that.githubPolledEventId) && Objects.equals(deliveryDestination, that.deliveryDestination) && Objects.equals(deliveryDestinationUrl, that.deliveryDestinationUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(githubPolledEventId, deliveryDestination, deliveryDestinationUrl);
    }

}
