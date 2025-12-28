package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookReceivedJpaRepository extends JpaRepository<WebhookReceived, Long> {

    boolean existsByWebhookId(String webhookId);

    WebhookReceived findFirstByOrderByIdDesc();

}
