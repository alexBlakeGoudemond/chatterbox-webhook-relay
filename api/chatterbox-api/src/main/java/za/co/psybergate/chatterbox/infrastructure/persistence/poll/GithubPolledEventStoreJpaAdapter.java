package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;

import java.util.List;

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
    public List<GithubPolledEvent> getLatestEvents(String repositoryFullName) {
        return repository.findByRepositoryFullNameAndEventStatus(repositoryFullName, EventStatus.RECEIVED);
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

    @Override
    public void setProcessedStatus(GithubPolledEvent polledEvent, EventStatus eventStatus) {
        polledEvent.setEventStatus(eventStatus);
        repository.save(polledEvent);
    }

    @Override
    public void setProcessedStatus(GithubPolledEvent polledEvent, EventStatus eventStatus, String responseDtoErrorResponse) {
        polledEvent.setEventStatus(eventStatus);
        polledEvent.setErrorMessage(responseDtoErrorResponse);
        repository.save(polledEvent);
    }

}
