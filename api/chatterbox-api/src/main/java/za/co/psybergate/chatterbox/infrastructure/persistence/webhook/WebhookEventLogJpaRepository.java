package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookEventLogJpaRepository extends JpaRepository<WebhookEventLog, Long> {

    List<WebhookEventLog> findAllByWebhookEventId(Long webhookEventId);

}
