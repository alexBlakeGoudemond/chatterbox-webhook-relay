package za.co.psybergate.chatterbox.infrastructure.persistence.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

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
    public boolean hasAlreadyBeenStored(String repositoryFullName, String webhookId) {
        try {
            return repository.findFirstByRepositoryFullNameAndWebhookIdOrderByIdDesc(repositoryFullName, webhookId);
        } catch (Exception e) {
            throw new ApplicationException("Unable to confirm if WebhookEvent exists", e);
        }
    }

    @Override
    public List<WebhookEvent> getLatestWebhooks(String repositoryFullName) {
        try {
            return repository.findByRepositoryFullNameAndEventStatusOrderByIdDesc(repositoryFullName, EventStatus.RECEIVED);
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve WebhookEvents", e);
        }
    }

    @Override
    public WebhookEvent storeWebhook(WebhookEvent webhook) {
        webhookLogger.logStoringEvent(webhook);
        try {
            return repository.save(webhook);
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
    public WebhookEventDeliveryLog storeDelivery(WebhookEventDeliveryLog webhookEventDeliveryLog) {
        webhookLogger.logDeliveringEvent(webhookEventDeliveryLog);
        try {
            return logRepository.save(webhookEventDeliveryLog);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store the Delivery information of the event", e);
        }
    }

    @Override
    public WebhookEventDeliveryLog storeDelivery(WebhookEvent webhookEvent, String destinationName, String destinationUrl) {
        WebhookEventDeliveryLog webhookEventDeliveryLog = new WebhookEventDeliveryLog(webhookEvent, destinationName, destinationUrl);
        return storeDelivery(webhookEventDeliveryLog);
    }

    @Override
    public void setProcessedStatus(WebhookEvent webhookEvent, EventStatus eventStatus) {
        webhookEvent.setEventStatus(eventStatus);
        try {
            repository.save(webhookEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the WebhookEvent", e);
        }
    }

    @Override
    public void setProcessedStatus(WebhookEvent webhookEvent, EventStatus eventStatus, String responseDtoErrorResponse) {
        webhookEvent.setEventStatus(eventStatus);
        webhookEvent.setErrorMessage(responseDtoErrorResponse);
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
        List<WebhookEvent> webhookEvents = getLatestWebhooks(repositoryName);
        if (webhookEvents.isEmpty()) {
            throw new ApplicationException("No WebhookEvents found for repository " + repositoryName);
        }
        return webhookEvents.getFirst();
    }

}
