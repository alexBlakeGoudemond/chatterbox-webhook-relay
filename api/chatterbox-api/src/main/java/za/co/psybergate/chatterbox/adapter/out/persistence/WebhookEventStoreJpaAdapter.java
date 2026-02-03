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
import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventDelivery;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceived;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.adapter.common.map.AdapterMapper;

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

    @Override
    public List<WebhookEventReceived> getLatestProcessedWebhooks(String repositoryFullName) {
        try {
            List<WebhookEvent> webhookEvents = repository.findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (webhookEvents.isEmpty()) {
                webhookLogger.logWebhookEventsEmpty(repositoryFullName);
                return List.of();
            }
            return webhookEvents.stream()
                    .map(AdapterMapper::mapToWebhookEventReceived)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public List<WebhookEventReceived> getUnprocessedWebhooks(String repositoryFullName) {
        try {
            List<WebhookEvent> webhookEvents = repository.findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.RECEIVED, Limit.of(5));
            if (webhookEvents.isEmpty()) {
                webhookLogger.logWebhookEventsEmpty(repositoryFullName);
                return List.of();
            }
            return webhookEvents.stream()
                    .map(AdapterMapper::mapToWebhookEventReceived)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public WebhookEventReceived storeWebhook(String uniqueId, OutboundEvent outboundEvent, RawEventPayload rawBody) {
        WebhookEvent webhook = new WebhookEvent(uniqueId, outboundEvent, rawBody.getAs(JsonNode.class));
        return storeWebhook(webhook);
    }

    private WebhookEventReceived storeWebhook(WebhookEvent webhook) {
        webhookLogger.logStoringEvent(webhook);
        try {
            WebhookEvent webhookEvent = repository.save(webhook);
            webhookLogger.logEventStored(webhook);
            return AdapterMapper.mapToWebhookEventReceived(webhookEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store WebhookEvent", e);
        }
    }

    @Override
    public WebhookEventDelivery storeSuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = AdapterMapper.mapToWebhookEventDeliveryLog(outboundEvent, destinationName, destinationUrl);
        return storeSuccessfulDelivery(webhookEventDeliveryLog);
    }

    @Override
    public WebhookEventDelivery storeUnsuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = AdapterMapper.mapToWebhookEventDeliveryLog(outboundEvent, destinationName, destinationUrl, WebhookEventStatus.PROCESSED_FAILURE);
        return storeSuccessfulDelivery(webhookEventDeliveryLog);
    }

    private WebhookEventDelivery storeSuccessfulDelivery(WebhookEventDeliveryLog webhookEventDeliveryLog) {
        webhookLogger.logDeliveringEvent(webhookEventDeliveryLog);
        try {
            WebhookEventDeliveryLog deliveryLog = logRepository.save(webhookEventDeliveryLog);
            webhookLogger.logEventDelivered(deliveryLog);
            return AdapterMapper.mapToWebhookEventDeliveryRecord(deliveryLog);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store the Delivery information of the event", e);
        }
    }

    @Override
    public void markProcessed(OutboundEvent outboundEvent, WebhookEventStatus webhookEventStatus) {
        WebhookEvent webhookEvent = AdapterMapper.mapToWebhookEvent(outboundEvent);
        webhookEvent.setId(outboundEvent.id());
        webhookEvent.setWebhookEventStatus(webhookEventStatus);
        webhookEvent.setProcessedAt(LocalDateTime.now());
        try {
            repository.save(webhookEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the WebhookEvent", e);
        }
    }

    @Override
    public WebhookEventReceived getWebhook(Long id) {
        WebhookEvent webhookEvent = repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
        return AdapterMapper.mapToWebhookEventReceived(webhookEvent);
    }

    @Override
    public List<WebhookEventDelivery> getDeliveryLogs(Long webhookEventId) {
        try {
            List<WebhookEventDeliveryLog> deliveryLogs = logRepository.findAllByWebhookEventId(webhookEventId);
            return deliveryLogs.stream()
                    .map(AdapterMapper::mapToWebhookEventDeliveryRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEventLogs", e);
        }
    }

    @Override
    public WebhookEventReceived getMostRecentWebhook(String repositoryName) {
        List<WebhookEventReceived> webhookEvents = getLatestProcessedWebhooks(repositoryName);
        if (webhookEvents.isEmpty()) {
            throw new ApplicationException("No WebhookEvents found for repository " + repositoryName);
        }
        return webhookEvents.getFirst();
    }

}
