package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookEventJpaRepository extends JpaRepository<WebhookEvent, Long> {

    boolean findFirstByRepositoryFullNameAndWebhookIdOrderByIdDesc(String repositoryFullName, String webhookId);

    WebhookEvent findFirstByRepositoryFullNameOrderByIdDesc(String repositoryFullName);

}
