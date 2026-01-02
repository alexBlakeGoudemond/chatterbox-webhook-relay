package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

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

    @Override
    public GithubPolledEvent storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        GithubPolledEvent webhook = new GithubPolledEvent(uniqueId, eventDto, rawBody);
        return storeEvent(webhook);
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
    public GithubPolledEventLog storeDelivery(GithubPolledEventLog polledEventLog){
        return logRepository.save(polledEventLog);
    }

    @Override
    public GithubPolledEventLog storeDelivery(GithubPolledEvent polledEvent, String exampleDestination, String exampleDestinationUrl){
        GithubPolledEventLog polledEventLog = new GithubPolledEventLog(polledEvent, exampleDestination, exampleDestinationUrl);
        return storeDelivery(polledEventLog);
    }

}
