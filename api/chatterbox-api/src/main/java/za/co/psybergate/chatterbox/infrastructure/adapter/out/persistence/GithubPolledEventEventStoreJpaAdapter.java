package za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStorePort;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.event.model.GithubPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.domain.event.model.GithubPolledEventDto;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.poll.GithubPolledEventDeliveryLog;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.poll.repository.GithubPolledEventJpaRepository;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.poll.repository.GithubPolledEventLogJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class GithubPolledEventEventStoreJpaAdapter implements GithubPolledEventStorePort {

    private final GithubPolledEventJpaRepository repository;

    private final GithubPolledEventLogJpaRepository logRepository;

    private final WebhookLogger webhookLogger;

    public GithubPolledEventEventStoreJpaAdapter(GithubPolledEventJpaRepository repository,
                                                 GithubPolledEventLogJpaRepository logRepository,
                                                 WebhookLogger webhookLogger) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.webhookLogger = webhookLogger;
    }

    public static GithubPolledEventDto mapToGithubPolledEventRecord(GithubPolledEvent polledEvent) {
        return new GithubPolledEventDto(
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

    private static GithubPolledEventDeliveryDto mapToGithubPolledEventDeliveryRecord(GithubPolledEventDeliveryLog deliveryLog) {
        return new GithubPolledEventDeliveryDto(
                deliveryLog.getId(),
                deliveryLog.getGithubPolledEventId(),
                deliveryLog.getDeliveryDestination(),
                deliveryLog.getDeliveryDestinationUrl(),
                deliveryLog.getEventStatus(),
                deliveryLog.getDeliveredAt()
        );
    }

    @Override
    public List<GithubPolledEventDto> getLatestProcessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logGithubPolledEventsEmpty(repositoryFullName);
                return List.of();
            }
            return githubPolledEvents.stream()
                    .map(GithubPolledEventEventStoreJpaAdapter::mapToGithubPolledEventRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public List<GithubPolledEventDto> getUnprocessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.RECEIVED, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logGithubPolledEventsEmpty(repositoryFullName);
                return List.of();
            }
            return githubPolledEvents.stream()
                    .map(GithubPolledEventEventStoreJpaAdapter::mapToGithubPolledEventRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public GithubPolledEventDto storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        GithubPolledEvent webhook = new GithubPolledEvent(uniqueId, eventDto, rawBody);
        return storeEvent(webhook);
    }

    private GithubPolledEventDto storeEvent(GithubPolledEvent event) {
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
    public GithubPolledEventDeliveryDto storeSuccessfulDelivery(GithubPolledEventDto polledEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(polledEvent.id(), destinationName, destinationUrl, EventStatus.PROCESSED_SUCCESS, LocalDateTime.now());
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    @Override
    public GithubPolledEventDeliveryDto storeUnsuccessfulDelivery(GithubPolledEventDto polledEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(polledEvent.id(), destinationName, destinationUrl, EventStatus.PROCESSED_FAILURE, LocalDateTime.now());
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    private GithubPolledEventDeliveryDto storeSuccessfulDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog) {
        webhookLogger.logDeliveringEvent(polledEventDeliveryLog);
        try {
            GithubPolledEventDeliveryLog deliveryLog = logRepository.save(polledEventDeliveryLog);
            webhookLogger.logEventDelivered(deliveryLog);
            return mapToGithubPolledEventDeliveryRecord(deliveryLog);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store Delivery information of the event", e);
        }
    }

    @Override
    public void setProcessedStatus(GithubPolledEventDto polledEventRecord, EventStatus eventStatus) {
        GithubPolledEvent polledEvent = new GithubPolledEvent(polledEventRecord.eventType(), polledEventRecord.sourceId(), polledEventRecord.repositoryFullName(), polledEventRecord.displayName(), polledEventRecord.senderName(), polledEventRecord.eventUrl(), polledEventRecord.eventUrlDisplayText(), polledEventRecord.extraDetail(), polledEventRecord.payload(), EventStatus.RECEIVED, LocalDateTime.now());
        polledEvent.setId(polledEventRecord.id());
        polledEvent.setEventStatus(eventStatus);
        polledEvent.setProcessedAt(LocalDateTime.now());
        try {
            repository.save(polledEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public GithubPolledEventDto getEvent(Long id) {
        GithubPolledEvent polledEvent = repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
        return mapToGithubPolledEventRecord(polledEvent);
    }

    @Override
    public List<GithubPolledEventDeliveryDto> getDeliveryLogs(Long id) {
        try {
            List<GithubPolledEventDeliveryLog> deliveryLogs = logRepository.findAllByGithubPolledEventId(id);
            return deliveryLogs.stream()
                    .map(GithubPolledEventEventStoreJpaAdapter::mapToGithubPolledEventDeliveryRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEventLogs", e);
        }
    }

    @Override
    public GithubPolledEventDto getMostRecentPolledEvent(String repositoryFullName) {
        List<GithubPolledEventDto> polledEvents = getLatestProcessedEvents(repositoryFullName);
        if (polledEvents.isEmpty()) {
            throw new ApplicationException("No polled events found for repository " + repositoryFullName);
        }
        return polledEvents.getFirst();
    }

}
