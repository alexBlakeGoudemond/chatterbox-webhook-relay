package za.co.psybergate.chatterbox.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStorePort;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.GithubPolledEventDeliveryLog;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.repository.GithubPolledEventJpaRepository;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.repository.GithubPolledEventLogJpaRepository;

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

    public static WebhookPolledEventReceivedDto mapToGithubPolledEventRecord(GithubPolledEvent polledEvent) {
        return new WebhookPolledEventReceivedDto(
                polledEvent.getId(),
                polledEvent.getRepositoryFullName(),
                polledEvent.getSourceId(),
                polledEvent.getWebhookEventType(),
                polledEvent.getDisplayName(),
                polledEvent.getSenderName(),
                polledEvent.getEventUrl(),
                polledEvent.getEventUrlDisplayText(),
                polledEvent.getExtraDetail(),
                polledEvent.getPayload(),
                polledEvent.getWebhookEventStatus(),
                polledEvent.getErrorMessage(),
                polledEvent.getFetchedAt(),
                polledEvent.getProcessedAt()
        );
    }

    private static WebhookPolledEventDeliveryDto mapToGithubPolledEventDeliveryRecord(GithubPolledEventDeliveryLog deliveryLog) {
        return new WebhookPolledEventDeliveryDto(
                deliveryLog.getId(),
                deliveryLog.getGithubPolledEventId(),
                deliveryLog.getDeliveryDestination(),
                deliveryLog.getDeliveryDestinationUrl(),
                deliveryLog.getWebhookEventStatus(),
                deliveryLog.getDeliveredAt()
        );
    }

    @Override
    public List<WebhookPolledEventReceivedDto> getLatestProcessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.PROCESSED_SUCCESS, Limit.of(5));
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
    public List<WebhookPolledEventReceivedDto> getUnprocessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.RECEIVED, Limit.of(5));
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
    public WebhookPolledEventReceivedDto storeEvent(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        GithubPolledEvent webhook = new GithubPolledEvent(uniqueId, eventDto, rawBody);
        return storeEvent(webhook);
    }

    private WebhookPolledEventReceivedDto storeEvent(GithubPolledEvent event) {
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
    public WebhookPolledEventDeliveryDto storeSuccessfulDelivery(WebhookPolledEventReceivedDto polledEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(polledEvent.id(), destinationName, destinationUrl, WebhookEventStatus.PROCESSED_SUCCESS, LocalDateTime.now());
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    @Override
    public WebhookPolledEventDeliveryDto storeUnsuccessfulDelivery(WebhookPolledEventReceivedDto polledEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(polledEvent.id(), destinationName, destinationUrl, WebhookEventStatus.PROCESSED_FAILURE, LocalDateTime.now());
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    private WebhookPolledEventDeliveryDto storeSuccessfulDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog) {
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
    public void setProcessedStatus(WebhookPolledEventReceivedDto polledEventRecord, WebhookEventStatus webhookEventStatus) {
        GithubPolledEvent polledEvent = new GithubPolledEvent(polledEventRecord.webhookEventType(), polledEventRecord.sourceId(), polledEventRecord.repositoryFullName(), polledEventRecord.displayName(), polledEventRecord.senderName(), polledEventRecord.eventUrl(), polledEventRecord.eventUrlDisplayText(), polledEventRecord.extraDetail(), polledEventRecord.payload(), WebhookEventStatus.RECEIVED, LocalDateTime.now());
        polledEvent.setId(polledEventRecord.id());
        polledEvent.setWebhookEventStatus(webhookEventStatus);
        polledEvent.setProcessedAt(LocalDateTime.now());
        try {
            repository.save(polledEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public WebhookPolledEventReceivedDto getEvent(Long id) {
        GithubPolledEvent polledEvent = repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
        return mapToGithubPolledEventRecord(polledEvent);
    }

    @Override
    public List<WebhookPolledEventDeliveryDto> getDeliveryLogs(Long id) {
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
    public WebhookPolledEventReceivedDto getMostRecentPolledEvent(String repositoryFullName) {
        List<WebhookPolledEventReceivedDto> polledEvents = getLatestProcessedEvents(repositoryFullName);
        if (polledEvents.isEmpty()) {
            throw new ApplicationException("No polled events found for repository " + repositoryFullName);
        }
        return polledEvents.getFirst();
    }

}
