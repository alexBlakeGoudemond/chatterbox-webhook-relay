package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.domain.api.EventStatus;

import java.util.List;

@Repository
public interface WebhookEventJpaRepository extends JpaRepository<WebhookEvent, Long> {

    boolean findFirstByRepositoryFullNameAndWebhookIdOrderByIdDesc(String repositoryFullName, String webhookId);

    List<WebhookEvent> findByRepositoryFullNameAndEventStatus(String repositoryFullName, EventStatus status);

}
