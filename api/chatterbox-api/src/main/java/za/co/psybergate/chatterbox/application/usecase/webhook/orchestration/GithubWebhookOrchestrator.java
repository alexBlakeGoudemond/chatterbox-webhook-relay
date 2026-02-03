package za.co.psybergate.chatterbox.application.usecase.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.domain.event.model.OutboundEvent;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.GithubWebhookPort;
import za.co.psybergate.chatterbox.application.port.out.github.delivery.GithubPollingPort;
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JsonConverter;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.application.port.in.validation.WebhookRequestValidatorPort;
import za.co.psybergate.chatterbox.application.domain.api.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookPolledEventReceivedDto;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventReceivedDto;
import za.co.psybergate.chatterbox.application.domain.event.notification.WebhookEventProcessed;
import za.co.psybergate.chatterbox.application.domain.event.model.RepositoryUpdates;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class GithubWebhookOrchestrator implements GithubWebhookPort {

    private final WebhookRequestValidatorPort webhookRequestValidatorPort;

    private final GithubEventMapper eventExtractor;

    private final JsonConverter jsonConverter;

    private final GithubPollingPort githubPollingPort;

    private final WebhookEventStorePort webhookEventStorePort;

    private final GithubPolledEventStorePort githubPolledEventStorePort;

    private final ApplicationEventPublisher publisher;

    private final WebhookLogger webhookLogger;

    @Override
    public WebhookEventReceivedDto process(String eventType, String deliveryId, JsonNode rawBody) {
        String repositoryName = jsonConverter.getRepositoryName(rawBody);
        webhookRequestValidatorPort.assertAcceptedRepository(repositoryName);
        webhookRequestValidatorPort.assertAcceptedEvent(eventType);
        OutboundEvent outboundEvent = getOutboundEvent(eventType, rawBody);
        WebhookEventReceivedDto webhookEvent = webhookEventStorePort.storeWebhook(deliveryId, outboundEvent, rawBody);
        publisher.publishEvent(new WebhookEventProcessed());
        return webhookEvent;
    }

    @Override
    public List<WebhookPolledEventReceivedDto> pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime) {
        return pollGithubForChanges(owner, repositoryName, lastReceivedTime, LocalDateTime.now());
    }

    @Override
    public List<WebhookPolledEventReceivedDto> pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        webhookRequestValidatorPort.assertAcceptedRepository(owner, repositoryName);
        RepositoryUpdates recentUpdates = githubPollingPort.getRecentUpdates(owner, repositoryName, fromDate, untilDate);
        String repositoryFullName = String.format("%s/%s", owner, repositoryName);
        List<WebhookPolledEventReceivedDto> updates = new ArrayList<>();
        for (Map.Entry<WebhookEventType, ArrayNode> entry : recentUpdates.getWebhookEventTypeDetails().entrySet()) {
            ArrayNode arrayNode = entry.getValue();
            WebhookEventType webhookEventType = entry.getKey();
            appendToArrayNode(arrayNode, "full_name", repositoryFullName);
            List<WebhookPolledEventReceivedDto> githubPolledEvents = storeEvents(webhookEventType, arrayNode);
            updates.addAll(githubPolledEvents);
        }
        return updates;
    }

    @Override
    public List<WebhookPolledEventReceivedDto> pollGithubForChanges(String repositoryFullName, LocalDateTime receivedAt) {
        String[] repositoryDetails = repositoryFullName.split("/");
        String owner = repositoryDetails[0];
        String repositoryName = repositoryDetails[1];
        return pollGithubForChanges(owner, repositoryName, receivedAt);
    }

    @Override
    public boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName) {
        LocalDateTime lastPersistedTime;
        try {
            WebhookEventReceivedDto latestWebhookEvent = webhookEventStorePort.getMostRecentWebhook(repositoryFullName);
            webhookLogger.logRunnerFoundPreviousWebhook(latestWebhookEvent);
            lastPersistedTime = latestWebhookEvent.receivedAt();
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousWebhooks(repositoryFullName);
            return false;
        }
        try {
            WebhookPolledEventReceivedDto latestGithubPolledEvent = githubPolledEventStorePort.getMostRecentPolledEvent(repositoryFullName);
            webhookLogger.logRunnerFoundPreviousPolledEvent(latestGithubPolledEvent);
            lastPersistedTime = getLastPersistedTime(lastPersistedTime, latestGithubPolledEvent.fetchedAt());
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousPolledEvents(repositoryFullName);
        }
        List<WebhookPolledEventReceivedDto> githubPolledEvents = pollGithubForChanges(repositoryFullName, lastPersistedTime);
        if (!githubPolledEvents.isEmpty()) {
            webhookLogger.logPolledEventsFound(githubPolledEvents, repositoryFullName, lastPersistedTime);
            return true;
        }
        webhookLogger.logNoPolledEventsFound(repositoryFullName, lastPersistedTime);
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private void appendToArrayNode(ArrayNode arrayNode, String jsonKey, String jsonValue) {
        for (JsonNode node : arrayNode) {
            if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                objectNode.put(jsonKey, jsonValue);
            }
        }
    }

    private List<WebhookPolledEventReceivedDto> storeEvents(WebhookEventType webhookEventType, ArrayNode arrayNode) {
        List<WebhookPolledEventReceivedDto> updates = new ArrayList<>();
        for (JsonNode jsonNode : arrayNode) {
            String uniqueId = webhookEventType.getUniqueId(jsonNode);
            OutboundEvent outboundEvent = getOutboundEvent(webhookEventType.name(), jsonNode);
            WebhookPolledEventReceivedDto polledEvent = githubPolledEventStorePort.storeEvent(uniqueId, outboundEvent, jsonNode);
            updates.add(polledEvent);
        }
        return updates;
    }

    private OutboundEvent getOutboundEvent(String eventType, JsonNode rawBody) {
        return eventExtractor.map(eventType, rawBody);
    }

    private LocalDateTime getLastPersistedTime(LocalDateTime persistedTime001, LocalDateTime persistedTime002) {
        if (persistedTime001.isAfter(persistedTime002)) {
            return persistedTime001;
        }
        return persistedTime002;
    }

}
