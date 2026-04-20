package com.webhook.relay.chatterbox.adapter.out.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.adapter.out.map.AdapterMapper;
import com.webhook.relay.chatterbox.adapter.out.persistence.poll.GithubPolledEvent;
import com.webhook.relay.chatterbox.adapter.out.persistence.poll.GithubPolledEventDeliveryLog;
import com.webhook.relay.chatterbox.adapter.out.persistence.poll.repository.GithubPolledEventJpaRepository;
import com.webhook.relay.chatterbox.adapter.out.persistence.poll.repository.GithubPolledEventLogJpaRepository;
import com.webhook.relay.chatterbox.application.common.exception.ApplicationException;
import com.webhook.relay.chatterbox.application.common.logging.WebhookLogger;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventStatus;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventDelivery;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventReceived;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;

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
    public List<WebhookPolledEventReceived> getLatestProcessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.PROCESSED_SUCCESS, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logPolledEventsEmpty(repositoryFullName);
                return List.of();
            }
            return githubPolledEvents.stream()
                    .map(AdapterMapper::mapToWebhookPolledEventReceived)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public List<WebhookPolledEventReceived> getUnprocessedEvents(String repositoryFullName) {
        try {
            List<GithubPolledEvent> githubPolledEvents = repository.findByRepositoryFullNameAndWebhookEventStatusOrderByIdDesc(repositoryFullName, WebhookEventStatus.RECEIVED, Limit.of(5));
            if (githubPolledEvents.isEmpty()) {
                webhookLogger.logPolledEventsEmpty(repositoryFullName);
                return List.of();
            }
            return githubPolledEvents.stream()
                    .map(AdapterMapper::mapToWebhookPolledEventReceived)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEvents", e);
        }
    }

    @Override
    public WebhookPolledEventReceived storeEvent(String uniqueId, OutboundEvent outboundEvent, RawEventPayload rawBody) {
        GithubPolledEvent webhook = new GithubPolledEvent(uniqueId, outboundEvent, rawBody.getAs(JsonNode.class));
        return storeEvent(webhook);
    }

    private WebhookPolledEventReceived storeEvent(GithubPolledEvent event) {
        webhookLogger.logStoringEvent(event);
        try {
            GithubPolledEvent polledEvent = repository.save(event);
            webhookLogger.logEventStored(polledEvent);
            return AdapterMapper.mapToWebhookPolledEventReceived(polledEvent);
        } catch (Exception e) {
            throw new ApplicationException("Unable to update the GithubPolledEvent", e);
        }
    }

    @Override
    public WebhookPolledEventDelivery storeSuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(outboundEvent.id(), destinationName, destinationUrl, WebhookEventStatus.PROCESSED_SUCCESS, LocalDateTime.now());
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    @Override
    public WebhookPolledEventDelivery storeUnsuccessfulDelivery(OutboundEvent outboundEvent, String destinationName, String destinationUrl) {
        GithubPolledEventDeliveryLog polledEventDeliveryLog = new GithubPolledEventDeliveryLog(outboundEvent.id(), destinationName, destinationUrl, WebhookEventStatus.PROCESSED_FAILURE, LocalDateTime.now());
        return storeSuccessfulDelivery(polledEventDeliveryLog);
    }

    private WebhookPolledEventDelivery storeSuccessfulDelivery(GithubPolledEventDeliveryLog polledEventDeliveryLog) {
        webhookLogger.logDeliveringEvent(polledEventDeliveryLog);
        try {
            GithubPolledEventDeliveryLog deliveryLog = logRepository.save(polledEventDeliveryLog);
            webhookLogger.logEventDelivered(deliveryLog);
            return AdapterMapper.mapToWebhookPolledEventDelivery(deliveryLog);
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
    public WebhookPolledEventReceived getEvent(Long id) {
        GithubPolledEvent polledEvent = repository.findById(id)
                .orElseThrow(() -> new ApplicationException("Unable to find WebhookEvent with ID " + id));
        return AdapterMapper.mapToWebhookPolledEventReceived(polledEvent);
    }

    @Override
    public List<WebhookPolledEventDelivery> getDeliveryLogs(Long id) {
        try {
            List<GithubPolledEventDeliveryLog> deliveryLogs = logRepository.findAllByGithubPolledEventId(id);
            return deliveryLogs.stream()
                    .map(AdapterMapper::mapToWebhookPolledEventDelivery)
                    .toList();
        } catch (Exception e) {
            throw new ApplicationException("Unable to retrieve GithubPolledEventLogs", e);
        }
    }

    @Override
    public WebhookPolledEventReceived getMostRecentPolledEvent(String repositoryFullName) {
        List<WebhookPolledEventReceived> polledEvents = getLatestProcessedEvents(repositoryFullName);
        if (polledEvents.isEmpty()) {
            throw new ApplicationException("No polled events found for repository " + repositoryFullName);
        }
        return polledEvents.getFirst();
    }

}
