package za.co.psybergate.chatterbox.adapter.out.persistence.webhook.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.WebhookEvent;

import java.util.List;

@Repository
public interface WebhookEventJpaRepository extends JpaRepository<WebhookEvent, Long> {

    List<WebhookEvent> findByRepositoryFullNameAndEventStatusOrderByIdDesc(String repositoryFullName, WebhookEventStatus webhookEventStatus, Limit limit);

}
