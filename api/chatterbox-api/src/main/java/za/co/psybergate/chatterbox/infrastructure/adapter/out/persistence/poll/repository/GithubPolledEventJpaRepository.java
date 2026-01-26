package za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.poll.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.poll.GithubPolledEvent;

import java.util.List;

@Repository
public interface GithubPolledEventJpaRepository extends JpaRepository<GithubPolledEvent, Long> {

    List<GithubPolledEvent> findByRepositoryFullNameAndEventStatusOrderByIdDesc(String repositoryFullName, EventStatus eventStatus, Limit limit);

}
