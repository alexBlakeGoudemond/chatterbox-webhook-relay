package za.co.psybergate.chatterbox.infrastructure.persistence.poll;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.event.GithubPolledEventDeliveryRecord;
import za.co.psybergate.chatterbox.domain.event.GithubPolledEventRecord;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLoggerImpl;

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
                                            WebhookLoggerImpl webhookLogger) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.webhookLogger = webhookLogger;
    }

    @Override
    public List<GithubPolledEventRecord> getLatestProcessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logGithubPolledEventsEmpty(repositoryFullName);
                return List.of();
            }
            return githubPolledEvents.stream()
                    .map(GithubPolledEventStoreJpaAdapter::mapToGithubPolledEventRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public List<GithubPolledEventRecord> getUnprocessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.RECEIVED, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logGithubPolledEventsEmpty(repositoryFullName);
            }
            return githubPolledEvents.stream()
                    .map(GithubPolledEventStoreJpaAdapter::mapToGithubPolledEventRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public GithubPolledEventRecord storeEvent(GithubPolledEvent event) {
        webhookLogger.logStoringEvent(event);
        try {
            GithubPolledEvent polledEvent = repository.save(event);
            webhookLogger.logEventStored(polledEvent);
            return mapToGithubPolledEventRecord(polledEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public GithubPolledEventRecord storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        GithubPolledEvent webhook = new GithubPolledEvent(uniqueId, eventDto, rawBody);
        return storeEvent(webhook);
    }

    @Override
    public GithubPolledEventDeliveryRecord storeSuccessfulDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog){
        webhookLogger.logDeliveringEvent(polledEventDeliveryLog);
        try {
            GithubPolledEventDeliveryLog deliveryLog = logRepository.save(polledEventDeliveryLog);
            webhookLogger.logEventDelivered(deliveryLog);
            return new GithubPolledEventDeliveryRecord(deliveryLog);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store Delivery information of the event", e);
        }
    }

    @Override
    public GithubPolledEventDeliveryRecord storeSuccessfulDelivery(GithubPolledEventRecord polledEvent, String destinationName, String destinationUrl){
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(polledEvent, destinationName, destinationUrl, EventStatus.PROCESSED_SUCCESS);
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    @Override
    public GithubPolledEventDeliveryRecord storeUnsuccessfulDelivery(GithubPolledEventRecord polledEventRecord, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(polledEventRecord, destinationName, destinationUrl, EventStatus.PROCESSED_FAILURE);
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    @Override
    public void setProcessedStatus(GithubPolledEventRecord polledEventRecord, EventStatus eventStatus) {
        GithubPolledEvent polledEvent = new GithubPolledEvent(polledEventRecord);
        polledEvent.setEventStatus(eventStatus);
        polledEvent.setProcessedAt(LocalDateTime.now());
        try {
            repository.save(polledEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public GithubPolledEventRecord getEvent(Long id) {
        GithubPolledEvent polledEvent = repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
        return mapToGithubPolledEventRecord(polledEvent);
    }

    public static GithubPolledEventRecord mapToGithubPolledEventRecord(GithubPolledEvent polledEvent) {
        return new GithubPolledEventRecord(
                polledEvent.getId(),
                polledEvent.getRepositoryFullName(),
                polledEvent.getSourceId(),
                polledEvent.getEventType(),
                polledEvent.getDisplayName(),
                polledEvent.getSenderName(),
                polledEvent.getEventUrl(),
                polledEvent.getEventUrlDisplayText(),
                polledEvent.getExtraDetail(),
                polledEvent.getPayload(),
                polledEvent.getEventStatus(),
                polledEvent.getErrorMessage(),
                polledEvent.getFetchedAt(),
                polledEvent.getProcessedAt()
        );
    }

    @Override
    public List<GithubPolledEventDeliveryRecord> getDeliveryLogs(Long id) {
        try {
            List<GithubPolledEventDeliveryLog> deliveryLogs = logRepository.findAllByGithubPolledEventId(id);
            return deliveryLogs.stream()
                    .map(GithubPolledEventDeliveryRecord::new)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEventLogs", e);
        }
    }

    @Override
    public GithubPolledEventRecord getMostRecentPolledEvent(String repositoryFullName) {
        List<GithubPolledEventRecord> polledEvents = getLatestProcessedEvents(repositoryFullName);
        if (polledEvents.isEmpty()) {
            throw new ApplicationException("No polled events found for repository " + repositoryFullName);
        }
        return polledEvents.getFirst();
    }

}
