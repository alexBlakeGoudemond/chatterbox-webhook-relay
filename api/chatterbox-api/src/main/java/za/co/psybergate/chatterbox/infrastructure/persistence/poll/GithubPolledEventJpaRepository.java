package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.util.List;

@Repository
public interface GithubPolledEventJpaRepository extends JpaRepository<GithubPolledEvent, Long> {

    boolean findFirstByRepositoryFullNameAndEventTypeAndSourceIdOrderByIdDesc(String repositoryFullName, EventType eventType, String sourceId);

    GithubPolledEvent findFirstByRepositoryFullNameOrderByIdDesc(String repositoryFullName);

    List<GithubPolledEvent> findByRepositoryFullNameAndEventStatus(String repositoryFullName, EventStatus status);

}
