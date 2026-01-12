package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional
public class GithubPolledEventStoreJpaAdapter implements GithubPolledStore {

    private final GithubPolledEventJpaRepository repository;

    private final GithubPolledEventLogJpaRepository logRepository;

    private final WebhookLogger webhookLogger;

    public GithubPolledEventStoreJpaAdapter(GithubPolledEventJpaRepository repository,
                                            GithubPolledEventLogJpaRepository logRepository,
                                            WebhookLogger webhookLogger) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.webhookLogger = webhookLogger;
    }

    @Override
    public boolean hasAlreadyBeenStored(String repositoryFullName, EventType eventType, String sourceId) {
        try {
            return repository.findFirstByRepositoryFullNameAndEventTypeAndSourceIdOrderByIdDesc(repositoryFullName, eventType, sourceId);
        } catch (Exception e) {
            throw new ApplicationException("Unable to confirm if GithubPolledEvent exists", e);
        }
    }

    @Override
    public List<GithubPolledEvent> getLatestEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logGithubPolledEventsEmpty(repositoryFullName);
            }
            return githubPolledEvents;
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public GithubPolledEvent storeEvent(GithubPolledEvent event) {
        webhookLogger.logStoringEvent(event);
        try {
            GithubPolledEvent save = repository.save(event);
            webhookLogger.logEventStored(save);
            return save;
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public GithubPolledEvent storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        GithubPolledEvent webhook = new GithubPolledEvent(uniqueId, eventDto, rawBody);
        return storeEvent(webhook);
    }

    @Override
    public GithubPolledEventDeliveryLog storeSuccessfulDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog){
        webhookLogger.logDeliveringEvent(polledEventDeliveryLog);
        try {
            GithubPolledEventDeliveryLog save = logRepository.save(polledEventDeliveryLog);
            webhookLogger.logEventDelivered(save);
            return save;
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store Delivery information of the event", e);
        }
    }

    @Override
    public GithubPolledEventDeliveryLog storeSuccessfulDelivery(GithubPolledEvent polledEvent, String destinationName, String destinationUrl){
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(polledEvent, destinationName, destinationUrl, EventStatus.PROCESSED_SUCCESS);
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    @Override
    public GithubPolledEventDeliveryLog storeUnsuccessfulDelivery(GithubPolledEvent polledEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(polledEvent, destinationName, destinationUrl, EventStatus.PROCESSED_FAILURE);
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    @Override
    public void setProcessedStatus(GithubPolledEvent polledEvent, EventStatus eventStatus) {
        polledEvent.setEventStatus(eventStatus);
        polledEvent.setProcessedAt(LocalDateTime.now());
        try {
            repository.save(polledEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public void setProcessedStatus(GithubPolledEvent polledEvent, EventStatus eventStatus, String responseDtoErrorResponse) {
        polledEvent.setEventStatus(eventStatus);
        polledEvent.setErrorMessage(responseDtoErrorResponse);
        try {
            repository.save(polledEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public GithubPolledEvent getEvent(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
    }

    @Override
    public List<GithubPolledEventDeliveryLog> getDeliveryLogs(Long id) {
        try {
            return logRepository.findAllByGithubPolledEventId(id);
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEventLogs", e);
        }
    }

    @Override
    public GithubPolledEvent getMostRecentPolledEvent(String repositoryFullName) {
        List<GithubPolledEvent> polledEvents = getLatestEvents(repositoryFullName);
        if (polledEvents.isEmpty()) {
            throw new ApplicationException("No polled events found for repository " + repositoryFullName);
        }
        return polledEvents.getFirst();
    }

}
