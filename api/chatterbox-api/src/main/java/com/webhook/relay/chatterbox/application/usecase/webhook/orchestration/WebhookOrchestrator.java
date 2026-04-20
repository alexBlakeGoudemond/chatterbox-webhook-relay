package com.webhook.relay.chatterbox.application.usecase.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.webhook.relay.chatterbox.application.common.exception.ApplicationException;
import com.webhook.relay.chatterbox.application.common.logging.MdcContext;
import com.webhook.relay.chatterbox.application.common.logging.WebhookLogger;
import com.webhook.relay.chatterbox.application.common.web.serialisation.JsonConverter;
import com.webhook.relay.chatterbox.application.domain.event.model.OutboundEvent;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.RepositoryUpdates;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.domain.event.notification.WebhookEventProcessed;
import com.webhook.relay.chatterbox.application.domain.exception.DomainException;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookEventReceived;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventReceived;
import com.webhook.relay.chatterbox.application.port.in.validation.WebhookRequestValidatorPort;
import com.webhook.relay.chatterbox.application.port.in.webhook.orchestration.WebhookOrchestratorPort;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import com.webhook.relay.chatterbox.application.port.out.webhook.mapper.OutboundEventMapperPort;
import com.webhook.relay.chatterbox.application.port.out.webhook.poll.WebhookPollingPort;
import com.webhook.relay.chatterbox.application.port.out.webhook.resolution.WebhookConfigurationResolverPort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class WebhookOrchestrator implements WebhookOrchestratorPort {

    private final WebhookRequestValidatorPort webhookRequestValidatorPort;

    private final OutboundEventMapperPort eventExtractor;

    private final JsonConverter jsonConverter;

    private final WebhookPollingPort webhookPollingPort;

    private final WebhookEventStorePort webhookEventStorePort;

    private final WebhookPolledEventStorePort webhookPolledEventStorePort;

    private final WebhookConfigurationResolverPort configurationResolver;

    private final ApplicationEventPublisher publisher;

    private final WebhookLogger webhookLogger;

    private final MdcContext mdcContext;

    @Override
    public WebhookEventReceived process(String eventType, String deliveryId, String rawBody) {
        JsonNode jsonNode = jsonConverter.getAsJson(rawBody);
        String repositoryName = jsonConverter.getRepositoryName(jsonNode);
        mdcContext.setRepositoryName(repositoryName);
        webhookRequestValidatorPort.assertAcceptedRepository(repositoryName);
        webhookRequestValidatorPort.assertAcceptedEvent(eventType);
        OutboundEvent outboundEvent = getOutboundEvent(eventType, jsonNode);
        WebhookEventReceived webhookEvent = webhookEventStorePort.storeWebhook(deliveryId, outboundEvent, RawEventPayload.of(jsonNode));
        publisher.publishEvent(new WebhookEventProcessed(webhookEvent));
        return webhookEvent;
    }

    @Override
    public List<WebhookPolledEventReceived> pollForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime) {
        return pollForChanges(owner, repositoryName, lastReceivedTime, LocalDateTime.now());
    }

    @Override
    public List<WebhookPolledEventReceived> pollForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        String repositoryFullName = String.format("%s/%s", owner, repositoryName);
        mdcContext.setRepositoryName(repositoryFullName);
        webhookRequestValidatorPort.assertAcceptedRepository(owner, repositoryName);
        RepositoryUpdates recentUpdates = webhookPollingPort.getRecentUpdates(owner, repositoryName, fromDate, untilDate);
        List<WebhookPolledEventReceived> updates = new ArrayList<>();
        for (Map.Entry<WebhookEventType, List<RawEventPayload>> entry : recentUpdates.getWebhookEventTypeDetails().entrySet()) {
            List<RawEventPayload> details = entry.getValue();
            WebhookEventType webhookEventType = entry.getKey();
            List<JsonNode> jsonNodes = details.stream()
                    .map(payload -> payload.getAs(JsonNode.class))
                    .toList();
            for (JsonNode node : jsonNodes) {
                appendToJsonNode(node, "full_name", repositoryFullName);
            }
            List<WebhookPolledEventReceived> githubPolledEvents = storeEvents(webhookEventType, jsonNodes);
            updates.addAll(githubPolledEvents);
        }
        return updates;
    }

    @Override
    public List<WebhookPolledEventReceived> pollForChanges(String repositoryFullName, LocalDateTime receivedAt) {
        String[] repositoryDetails = repositoryFullName.split("/");
        String owner = repositoryDetails[0];
        String repositoryName = repositoryDetails[1];
        return pollForChanges(owner, repositoryName, receivedAt);
    }

    @Override
    public boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName) {
        LocalDateTime lastPersistedTime;
        try {
            WebhookEventReceived latestWebhookEvent = webhookEventStorePort.getMostRecentWebhook(repositoryFullName);
            webhookLogger.logRunnerFoundPreviousWebhook(latestWebhookEvent);
            lastPersistedTime = latestWebhookEvent.receivedAt();
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousWebhooks(repositoryFullName);
            return false;
        }
        try {
            WebhookPolledEventReceived latestGithubPolledEvent = webhookPolledEventStorePort.getMostRecentPolledEvent(repositoryFullName);
            webhookLogger.logRunnerFoundPreviousPolledEvent(latestGithubPolledEvent);
            lastPersistedTime = getLastPersistedTime(lastPersistedTime, latestGithubPolledEvent.fetchedAt());
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousPolledEvents(repositoryFullName);
        }
        List<WebhookPolledEventReceived> githubPolledEvents = pollForChanges(repositoryFullName, lastPersistedTime);
        if (!githubPolledEvents.isEmpty()) {
            webhookLogger.logPolledEventsFound(githubPolledEvents, repositoryFullName, lastPersistedTime);
            return true;
        }
        webhookLogger.logNoPolledEventsFound(repositoryFullName, lastPersistedTime);
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private void appendToJsonNode(JsonNode node, String jsonKey, String jsonValue) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.put(jsonKey, jsonValue);
        }
    }

    private List<WebhookPolledEventReceived> storeEvents(WebhookEventType webhookEventType, List<JsonNode> jsonNodes) {
        List<WebhookPolledEventReceived> updates = new ArrayList<>();
        for (JsonNode jsonNode : jsonNodes) {
            String uniqueId = getWebhookEventUniqueId(webhookEventType, jsonNode);
            OutboundEvent outboundEvent = getOutboundEvent(webhookEventType.name(), jsonNode);
            WebhookPolledEventReceived polledEvent = webhookPolledEventStorePort.storeEvent(uniqueId, outboundEvent, RawEventPayload.of(jsonNode));
            updates.add(polledEvent);
        }
        return updates;
    }

    private String getWebhookEventUniqueId(WebhookEventType webhookEventType, JsonNode jsonNode) {
        return switch (webhookEventType) {
            case POLL_COMMIT -> jsonNode.get("sha").asText();
            case POLL_PULL_REQUEST -> jsonNode.get("merge_commit_sha").asText();
            default -> throw new DomainException("Unable to find UniqueID; Unknown event type " + this);
        };
    }

    private OutboundEvent getOutboundEvent(String eventType, JsonNode rawBody) {
        return eventExtractor.map(eventType, RawEventPayload.of(rawBody));
    }

    private LocalDateTime getLastPersistedTime(LocalDateTime persistedTime001, LocalDateTime persistedTime002) {
        if (persistedTime001.isAfter(persistedTime002)) {
            return persistedTime001;
        }
        return persistedTime002;
    }

    @Override
    public List<String> getAllRepositories() {
        return configurationResolver.getAllRepositories();
    }

}
