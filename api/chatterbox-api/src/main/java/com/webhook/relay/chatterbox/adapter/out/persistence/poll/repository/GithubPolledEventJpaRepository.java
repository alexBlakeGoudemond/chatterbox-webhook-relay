package com.webhook.relay.chatterbox.adapter.out.persistence.poll.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webhook.relay.chatterbox.adapter.out.persistence.poll.GithubPolledEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventStatus;

import java.util.List;

@Repository
public interface GithubPolledEventJpaRepository extends JpaRepository<GithubPolledEvent, Long> {

    List<GithubPolledEvent> findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(String repositoryFullName, WebhookEventStatus webhookEventStatus, Limit limit);

}
