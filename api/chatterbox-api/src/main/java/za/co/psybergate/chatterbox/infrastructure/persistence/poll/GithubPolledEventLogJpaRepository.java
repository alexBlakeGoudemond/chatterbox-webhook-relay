package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventLog;

@Repository
public interface GithubPolledEventLogJpaRepository extends JpaRepository<WebhookEventLog, Long> {

}
