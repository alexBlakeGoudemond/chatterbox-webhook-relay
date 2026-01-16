package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.persistence.dto.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.domain.persistence.dto.WebhookEventDto;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional
public class WebhookEventStoreJpaAdapter implements WebhookReceivedStore {

    private final WebhookEventJpaRepository repository;

    private final WebhookEventLogJpaRepository logRepository;

    private final WebhookLogger webhookLogger;

    public WebhookEventStoreJpaAdapter(WebhookEventJpaRepository repository,
                                       WebhookEventLogJpaRepository logRepository,
                                       WebhookLogger webhookLogger) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.webhookLogger = webhookLogger;
    }

    @Override
    public List<WebhookEventDto> getLatestProcessedWebhooks(String repositoryFullName) {
        try {
            List<WebhookEvent> webhookEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (webhookEvents.isEmpty()) {
                webhookLogger.logWebhookEventsEmpty(repositoryFullName);
            }
            return webhookEvents.stream()
                    .map(WebhookEventStoreJpaAdapter::mapToWebhookEventRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public List<WebhookEventDto> getUnprocessedWebhooks(String repositoryFullName) {
        try {
            List<WebhookEvent> webhookEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.RECEIVED, Limit.of(5));
            if (webhookEvents.isEmpty()) {
                webhookLogger.logWebhookEventsEmpty(repositoryFullName);
            }
            return webhookEvents.stream()
                    .map(WebhookEventStoreJpaAdapter::mapToWebhookEventRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public WebhookEventDto storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        WebhookEvent webhook = new WebhookEvent(uniqueId, eventDto, rawBody);
        return storeWebhook(webhook);
    }

    private WebhookEventDto storeWebhook(WebhookEvent webhook) {
        webhookLogger.logStoringEvent(webhook);
        try {
            WebhookEvent webhookEvent = repository.save(webhook);
            webhookLogger.logEventStored(webhook);
            return mapToWebhookEventRecord(webhookEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store WebhookEvent", e);
        }
    }

    public static WebhookEventDto mapToWebhookEventRecord(WebhookEvent webhookEvent) {
        return new WebhookEventDto(webhookEvent.getId(),
                webhookEvent.getRepositoryFullName(),
                webhookEvent.getWebhookId(),
                webhookEvent.getEventType(),
                webhookEvent.getDisplayName(),
                webhookEvent.getSenderName(),
                webhookEvent.getEventUrl(),
                webhookEvent.getEventUrlDisplayText(),
                webhookEvent.getExtraDetail(),
                webhookEvent.getPayload(),
                webhookEvent.getEventStatus(),
                webhookEvent.getErrorMessage(),
                webhookEvent.getReceivedAt(),
                webhookEvent.getProcessedAt());
    }

    @Override
    public WebhookEventDeliveryDto storeSuccessfulDelivery(WebhookEventDto webhookEventDto, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = new WebhookEventDeliveryLog(webhookEventDto, destinationName, destinationUrl, EventStatus.PROCESSED_SUCCESS);
        return storeSuccessfulDelivery(webhookEventDeliveryLog);
    }

    @Override
    public WebhookEventDeliveryDto storeUnsuccessfulDelivery(WebhookEventDto webhookEventDto, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = new WebhookEventDeliveryLog(webhookEventDto, destinationName, destinationUrl, EventStatus.PROCESSED_FAILURE);
        return storeSuccessfulDelivery(webhookEventDeliveryLog);
    }

    private WebhookEventDeliveryDto storeSuccessfulDelivery(WebhookEventDeliveryLog webhookEventDeliveryLog) {
        webhookLogger.logDeliveringEvent(webhookEventDeliveryLog);
        try {
            WebhookEventDeliveryLog deliveryLog = logRepository.save(webhookEventDeliveryLog);
            webhookLogger.logEventDelivered(deliveryLog);
            return mapToWebhookEventDeliveryRecord(deliveryLog);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store the Delivery information of the event", e);
        }
    }

    @Override
    public void setProcessedStatus(WebhookEventDto webhookEventDto, EventStatus eventStatus) {
        WebhookEvent webhookEvent = new WebhookEvent(webhookEventDto);
        webhookEvent.setEventStatus(eventStatus);
        webhookEvent.setProcessedAt(LocalDateTime.now());
        try {
            repository.save(webhookEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the WebhookEvent", e);
        }
    }

    @Override
    public WebhookEventDto getWebhook(Long id) {
        WebhookEvent webhookEvent = repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
        return mapToWebhookEventRecord(webhookEvent);
    }

    @Override
    public List<WebhookEventDeliveryDto> getDeliveryLogs(Long webhookEventId) {
        try {
            List<WebhookEventDeliveryLog> deliveryLogs = logRepository.findAllByWebhookEventId(webhookEventId);
            return deliveryLogs.stream()
                    .map(WebhookEventStoreJpaAdapter::mapToWebhookEventDeliveryRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEventLogs", e);
        }
    }

    // TODO BlakeGoudemond 2026/01/16 | place in mapper class, like MapStruct?
    private static WebhookEventDeliveryDto mapToWebhookEventDeliveryRecord(WebhookEventDeliveryLog deliveryLog) {
        return new WebhookEventDeliveryDto(
                deliveryLog.getId(),
                deliveryLog.getWebhookEventId(),
                deliveryLog.getDeliveryDestination(),
                deliveryLog.getDeliveryDestinationUrl(),
                deliveryLog.getEventStatus(),
                deliveryLog.getDeliveredAt()
        );
    }

    @Override
    public WebhookEventDto getMostRecentWebhook(String repositoryName) {
        List<WebhookEventDto> webhookEvents = getLatestProcessedWebhooks(repositoryName);
        if (webhookEvents.isEmpty()) {
            throw new ApplicationException("No WebhookEvents found for repository " + repositoryName);
        }
        return webhookEvents.getFirst();
    }

}
