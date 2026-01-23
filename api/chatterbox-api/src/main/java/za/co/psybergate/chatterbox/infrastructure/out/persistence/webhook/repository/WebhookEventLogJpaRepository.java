package za.co.psybergate.chatterbox.infrastructure.out.persistence.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.infrastructure.out.persistence.webhook.WebhookEventDeliveryLog;

import java.util.List;

@Repository
public interface WebhookEventLogJpaRepository extends JpaRepository<WebhookEventDeliveryLog, Long> {

    List<WebhookEventDeliveryLog> findAllByWebhookEventId(Long webhookEventId);

}
