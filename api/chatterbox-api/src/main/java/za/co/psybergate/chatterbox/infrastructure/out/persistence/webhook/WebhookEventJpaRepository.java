package za.co.psybergate.chatterbox.infrastructure.out.persistence.webhook;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.domain.api.EventStatus;

import java.util.List;

@Repository
public interface WebhookEventJpaRepository extends JpaRepository<WebhookEvent, Long> {

    List<WebhookEvent> findByRepositoryFullNameAndEventStatusOrderByIdDesc(String repositoryFullName, EventStatus eventStatus, Limit limit);

}
