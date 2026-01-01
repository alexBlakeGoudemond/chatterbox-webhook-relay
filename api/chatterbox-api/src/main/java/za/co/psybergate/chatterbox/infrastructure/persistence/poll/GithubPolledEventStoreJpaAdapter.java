package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;

@Component
@Transactional
public class GithubPolledEventStoreJpaAdapter implements GithubPolledStore {

    private final GithubPolledEventJpaRepository repository;

    private final GithubPolledEventLogJpaRepository logRepository;

    public GithubPolledEventStoreJpaAdapter(GithubPolledEventJpaRepository repository,
                                            GithubPolledEventLogJpaRepository logRepository) {
        this.repository = repository;
        this.logRepository = logRepository;
    }

    @Override
    public GithubPolledEvent storeEvent(GithubPolledEvent event) {
        return repository.save(event);
    }

    // TODO BlakeGoudemond 2026/01/01 | log storage
    @Override
    public GithubPolledEvent storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        GithubPolledEvent webhook = new GithubPolledEvent(uniqueId, eventDto, rawBody);
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

    @Override
    public void logDelivery(GithubPolledEvent polledEvent){
        throw new ApplicationException("Not implemented");
    }

}
