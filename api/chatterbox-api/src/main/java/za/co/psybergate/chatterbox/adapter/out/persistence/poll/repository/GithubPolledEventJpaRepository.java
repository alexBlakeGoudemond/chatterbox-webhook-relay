package za.co.psybergate.chatterbox.adapter.out.persistence.poll.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.GithubPolledEvent;

import java.util.List;

@Repository
public interface GithubPolledEventJpaRepository extends JpaRepository<GithubPolledEvent, Long> {

    List<GithubPolledEvent> findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(String repositoryFullName, WebhookEventStatus webhookEventStatus, Limit limit);

}
