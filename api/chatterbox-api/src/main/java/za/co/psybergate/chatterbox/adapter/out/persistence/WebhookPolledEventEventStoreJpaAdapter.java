package za.co.psybergate.chatterbox.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventDeliveryDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;

import za.co.psybergate.chatterbox.adapter.out.persistence.poll.GithubPolledEvent;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.GithubPolledEventDeliveryLog;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.repository.GithubPolledEventJpaRepository;
import za.co.psybergate.chatterbox.adapter.out.persistence.poll.repository.GithubPolledEventLogJpaRepository;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventStatus;
import za.co.psybergate.chatterbox.adapter.common.map.AdapterMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WebhookPolledEventEventStoreJpaAdapter implements WebhookPolledEventStorePort {

    private final GithubPolledEventJpaRepository repository;

    private final GithubPolledEventLogJpaRepository logRepository;

    private final WebhookLogger webhookLogger;

    public WebhookPolledEventEventStoreJpaAdapter(GithubPolledEventJpaRepository repository,
                                                  GithubPolledEventLogJpaRepository logRepository,
                                                  WebhookLogger webhookLogger) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.webhookLogger = webhookLogger;
    }

    @Override
    public List<WebhookPolledEventReceivedDto> getLatestProcessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logPolledEventsEmpty(repositoryFullName);
                return List.of();
            }
            return githubPolledEvents.stream()
                    .map(AdapterMapper::mapToGithubPolledEventRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public List<WebhookPolledEventReceivedDto> getUnprocessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.RECEIVED, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logPolledEventsEmpty(repositoryFullName);
                return List.of();
            }
            return githubPolledEvents.stream()
                    .map(AdapterMapper::mapToGithubPolledEventRecord)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public WebhookPolledEventReceivedDto storeEvent(String uniqueId, OutboundEvent outboundEvent, RawEventPayload rawBody) {
        GithubPolledEvent webhook = new GithubPolledEvent(uniqueId, outboundEvent, rawBody.getAs(JsonNode.class));
        return storeEvent(webhook);
    }

    private WebhookPolledEventReceivedDto storeEvent(GithubPolledEvent event) {
        webhookLogger.logStoringEvent(event);
        try {
            GithubPolledEvent polledEvent = repository.save(event);
            webhookLogger.logEventStored(polledEvent);
            return AdapterMapper.mapToGithubPolledEventRecord(polledEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public WebhookPolledEventDeliveryDto storeSuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(outboundEvent.id(), destinationName, destinationUrl, WebhookEventStatus.PROCESSED_SUCCESS, LocalDateTime.now());
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    @Override
    public WebhookPolledEventDeliveryDto storeUnsuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(outboundEvent.id(), destinationName, destinationUrl, WebhookEventStatus.PROCESSED_FAILURE, LocalDateTime.now());
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    private WebhookPolledEventDeliveryDto storeSuccessfulDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog) {
        webhookLogger.logDeliveringEvent(polledEventDeliveryLog);
        try {
            GithubPolledEventDeliveryLog deliveryLog = logRepository.save(polledEventDeliveryLog);
            webhookLogger.logEventDelivered(deliveryLog);
            return AdapterMapper.mapToGithubPolledEventDeliveryRecord(deliveryLog);
        } catch (Exception e) {
            throw new ApplicationException("Unable to Store Delivery information of the event", e);
        }
    }

    @Override
    public void markProcessed(OutboundEvent outboundEvent, WebhookEventStatus webhookEventStatus) {
        GithubPolledEvent polledEvent = AdapterMapper.mapToGithubPolledEvent(outboundEvent);
        polledEvent.setId(outboundEvent.id());
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
        return AdapterMapper.mapToGithubPolledEventRecord(polledEvent);
    }

    @Override
    public List<WebhookPolledEventDeliveryDto> getDeliveryLogs(Long id) {
        try {
            List<GithubPolledEventDeliveryLog> deliveryLogs = logRepository.findAllByGithubPolledEventId(id);
            return deliveryLogs.stream()
                    .map(AdapterMapper::mapToGithubPolledEventDeliveryRecord)
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
