package za.co.psybergate.chatterbox.infrastructure.out.persistence.poll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.infrastructure.out.persistence.poll.GithubPolledEventDeliveryLog;

import java.util.List;

@Repository
public interface GithubPolledEventLogJpaRepository extends JpaRepository<GithubPolledEventDeliveryLog, Long> {

    List<GithubPolledEventDeliveryLog> findAllByGithubPolledEventId(Long githubPolledEventId);

}
