package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubPolledEventLogJpaRepository extends JpaRepository<GithubPolledEventLog, Long> {

    List<GithubPolledEventLog> findAllByGithubPolledEventId(Long githubPolledEventId);

}
