package za.co.psybergate.chatterbox.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.WebhookEvent;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.WebhookEventDeliveryLog;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.repository.WebhookEventJpaRepository;
import za.co.psybergate.chatterbox.adapter.out.persistence.webhook.repository.WebhookEventLogJpaRepository;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;

import java.time.LocalDateTime;
import java.util.List;

// TODO BlakeGoudemond 2026/01/16 | in time, consider a generic / polymorphic version of this as the code is very similar to GithubPolledEventStoreJpaAdapter
@Component
public class WebhookEventStoreJpaAdapter implements WebhookEventStorePort {

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

    public static WebhookEventReceivedDto mapToWebhookEventReceivedDto(WebhookEvent webhookEvent) {
        return new WebhookEventReceivedDto(webhookEvent.getId(),
                webhookEvent.getRepositoryFullName(),
                webhookEvent.getWebhookId(),
                webhookEvent.getWebhookEventType(),
                webhookEvent.getDisplayName(),
                webhookEvent.getSenderName(),
                webhookEvent.getEventUrl(),
                webhookEvent.getEventUrlDisplayText(),
                webhookEvent.getExtraDetail(),
                webhookEvent.getPayload(),
                webhookEvent.getWebhookEventStatus(),
                webhookEvent.getErrorMessage(),
                webhookEvent.getReceivedAt(),
                webhookEvent.getProcessedAt());
    }

    private static WebhookEventDeliveryDto mapToWebhookEventDeliveryRecord(WebhookEventDeliveryLog deliveryLog) {
        return new WebhookEventDeliveryDto(
                deliveryLog.getId(),
                deliveryLog.getWebhookEventId(),
                deliveryLog.getDeliveryDestination(),
                deliveryLog.getDeliveryDestinationUrl(),
                deliveryLog.getWebhookEventStatus(),
                deliveryLog.getDeliveredAt()
        );
    }

    @Override
    public List<WebhookEventReceivedDto> getLatestProcessedWebhooks(String repositoryFullName) {
        try {
            List<WebhookEvent> webhookEvents = repository.findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (webhookEvents.isEmpty()) {
                webhookLogger.logWebhookEventsEmpty(repositoryFullName);
                return List.of();
            }
            return webhookEvents.stream()
                    .map(WebhookEventStoreJpaAdapter::mapToWebhookEventReceivedDto)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public List<WebhookEventReceivedDto> getUnprocessedWebhooks(String repositoryFullName) {
        try {
            List<WebhookEvent> webhookEvents = repository.findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.RECEIVED, Limit.of(5));
            if (webhookEvents.isEmpty()) {
                webhookLogger.logWebhookEventsEmpty(repositoryFullName);
                return List.of();
            }
            return webhookEvents.stream()
                    .map(WebhookEventStoreJpaAdapter::mapToWebhookEventReceivedDto)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public WebhookEventReceivedDto storeWebhook(String uniqueId, OutboundEvent outboundEvent, JsonNode rawBody) {
        WebhookEvent webhook = new WebhookEvent(uniqueId, outboundEvent, rawBody);
        return storeWebhook(webhook);
    }

    private WebhookEventReceivedDto storeWebhook(WebhookEvent webhook) {
        webhookLogger.logStoringEvent(webhook);
        try {
            WebhookEvent webhookEvent = repository.save(webhook);
            webhookLogger.logEventStored(webhook);
            return mapToWebhookEventReceivedDto(webhookEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store WebhookEvent", e);
        }
    }

    @Override
    public WebhookEventDeliveryDto storeSuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = mapToWebhookEventDeliveryLog(outboundEvent, destinationName, destinationUrl);
        return storeSuccessfulDelivery(webhookEventDeliveryLog);
    }

    @Override
    public WebhookEventDeliveryDto storeUnsuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = mapToWebhookEventDeliveryLog(outboundEvent, destinationName, destinationUrl, WebhookEventStatus.PROCESSED_FAILURE);
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
    public void markProcessed(OutboundEvent outboundEvent, WebhookEventStatus webhookEventStatus) {
        WebhookEvent webhookEvent = mapToWebhookEvent(outboundEvent);
        webhookEvent.setId(outboundEvent.technicalId());
        webhookEvent.setWebhookEventStatus(webhookEventStatus);
        webhookEvent.setProcessedAt(LocalDateTime.now());
        try {
            repository.save(webhookEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the WebhookEvent", e);
        }
    }

    private WebhookEventDeliveryLog mapToWebhookEventDeliveryLog(OutboundEvent outboundEvent, String destinationName, String destinationUrl, WebhookEventStatus processedStatus) {
        return new WebhookEventDeliveryLog(outboundEvent.technicalId(),
                destinationName,
                destinationUrl,
                processedStatus,
                LocalDateTime.now());
    }

    private WebhookEventDeliveryLog mapToWebhookEventDeliveryLog(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        return mapToWebhookEventDeliveryLog(outboundEvent,
                destinationName,
                destinationUrl,
                WebhookEventStatus.PROCESSED_SUCCESS);
    }

    @Override
    public WebhookEventReceivedDto getWebhook(Long id) {
        WebhookEvent webhookEvent = repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
        return mapToWebhookEventReceivedDto(webhookEvent);
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

    @Override
    public WebhookEventReceivedDto getMostRecentWebhook(String repositoryName) {
        List<WebhookEventReceivedDto> webhookEvents = getLatestProcessedWebhooks(repositoryName);
        if (webhookEvents.isEmpty()) {
            throw new ApplicationException("No WebhookEvents found for repository " + repositoryName);
        }
        return webhookEvents.getFirst();
    }

    private WebhookEvent mapToWebhookEvent(OutboundEvent outboundEvent) {
        return new WebhookEvent(
                outboundEvent.uniqueId(),
                outboundEvent.repository(),
                outboundEvent.type(),
                outboundEvent.title(),
                outboundEvent.actor(),
                outboundEvent.url(),
                outboundEvent.displayText(),
                outboundEvent.extra(),
                outboundEvent.payload(),
                WebhookEventStatus.RECEIVED,
                LocalDateTime.now());
    }

}
