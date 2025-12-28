package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

@Component
@Transactional
public class GithubPolledEventStoreJpaAdapter implements GithubPolledStore {

    private final GithubPolledEventJpaRepository repository;

    public GithubPolledEventStoreJpaAdapter(GithubPolledEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public GithubPolledEvent storeEvent(GithubPolledEvent event) {
        return repository.save(event);
    }

    @Override
    public GithubPolledEvent storeEvent(GithubEventDto eventDto, JsonNode rawBody) {
        throw new ApplicationException("Not implemented");
    }

    @Override
    public boolean hasAlreadyBeenStored(String repositoryFullName, EventType eventType, String sourceId) {
        return repository.findFirstByRepositoryFullNameAndEventTypeAndSourceIdOrderByIdDesc(repositoryFullName, eventType, sourceId);
    }

    @Override
    public GithubPolledEvent getLatestEvent(String repositoryFullName) {
        return repository.findFirstByRepositoryFullNameOrderByIdDesc(repositoryFullName);
    }

}
