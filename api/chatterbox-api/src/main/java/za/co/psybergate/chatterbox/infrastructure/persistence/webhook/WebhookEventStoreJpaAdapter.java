package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

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
    public List<WebhookEvent> getLatestProcessedWebhooks(String repositoryFullName) {
        try {
            List<WebhookEvent> webhookEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (webhookEvents.isEmpty()) {
                webhookLogger.logWebhookEventsEmpty(repositoryFullName);
            }
            return webhookEvents;
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public List<WebhookEvent> getUnprocessedWebhooks(String repositoryFullName) {
        try {
            List<WebhookEvent> webhookEvents = repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.RECEIVED, Limit.of(5));
            if (webhookEvents.isEmpty()) {
                webhookLogger.logWebhookEventsEmpty(repositoryFullName);
            }
            return webhookEvents;
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public WebhookEvent storeWebhook(WebhookEvent webhook) {
        webhookLogger.logStoringEvent(webhook);
        try {
            WebhookEvent save = repository.save(webhook);
            webhookLogger.logEventStored(webhook);
            return save;
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store WebhookEvent", e);
        }
    }

    @Override
    public WebhookEvent storeWebhook(String uniqueId, GithubEventDto eventDto, JsonNode rawBody) {
        WebhookEvent webhook = new WebhookEvent(uniqueId, eventDto, rawBody);
        return storeWebhook(webhook);
    }

    @Override
    public WebhookEventDeliveryLog storeSuccessfulDelivery(WebhookEventDeliveryLog webhookEventDeliveryLog) {
        webhookLogger.logDeliveringEvent(webhookEventDeliveryLog);
        try {
            WebhookEventDeliveryLog save = logRepository.save(webhookEventDeliveryLog);
            webhookLogger.logEventDelivered(save);
            return save;
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store the Delivery information of the event", e);
        }
    }

    @Override
    public WebhookEventDeliveryLog storeSuccessfulDelivery(WebhookEvent webhookEvent, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = new WebhookEventDeliveryLog(webhookEvent, destinationName, destinationUrl, EventStatus.PROCESSED_SUCCESS);
        return storeSuccessfulDelivery(webhookEventDeliveryLog);
    }

    @Override
    public WebhookEventDeliveryLog storeUnsuccessfulDelivery(WebhookEvent webhookEvent, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = new WebhookEventDeliveryLog(webhookEvent, destinationName, destinationUrl, EventStatus.PROCESSED_FAILURE);
        return storeSuccessfulDelivery(webhookEventDeliveryLog);
    }

    @Override
    public void setProcessedStatus(WebhookEvent webhookEvent, EventStatus eventStatus) {
        webhookEvent.setEventStatus(eventStatus);
        webhookEvent.setProcessedAt(LocalDateTime.now());
        try {
            repository.save(webhookEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the WebhookEvent", e);
        }
    }

    @Override
    public WebhookEvent getWebhook(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
    }

    @Override
    public List<WebhookEventDeliveryLog> getDeliveryLogs(Long webhookEventId) {
        try {
            return logRepository.findAllByWebhookEventId(webhookEventId);
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEventLogs", e);
        }
    }

    @Override
    public WebhookEvent getMostRecentWebhook(String repositoryName) {
        List<WebhookEvent> webhookEvents = getLatestProcessedWebhooks(repositoryName);
        if (webhookEvents.isEmpty()) {
            throw new ApplicationException("No WebhookEvents found for repository " + repositoryName);
        }
        return webhookEvents.getFirst();
    }

}
