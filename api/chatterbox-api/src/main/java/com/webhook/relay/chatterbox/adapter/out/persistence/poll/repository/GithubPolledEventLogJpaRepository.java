package com.webhook.relay.chatterbox.adapter.out.persistence.poll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.webhook.relay.chatterbox.adapter.out.persistence.poll.GithubPolledEventDeliveryLog;

import java.util.List;

@Repository
public interface GithubPolledEventLogJpaRepository extends JpaRepository<GithubPolledEventDeliveryLog, Long> {

    List<GithubPolledEventDeliveryLog> findAllByGithubPolledEventId(Long githubPolledEventId);

}
