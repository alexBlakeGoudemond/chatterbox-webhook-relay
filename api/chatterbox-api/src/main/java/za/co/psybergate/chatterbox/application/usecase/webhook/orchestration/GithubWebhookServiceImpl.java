package za.co.psybergate.chatterbox.application.usecase.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.GithubWebhookService;
import za.co.psybergate.chatterbox.application.port.out.github.delivery.GithubPollingService;
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStore;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStore;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.usecase.web.serialisation.JsonConverter;
import za.co.psybergate.chatterbox.application.usecase.webhook.mapper.GithubEventMapper;
import za.co.psybergate.chatterbox.application.usecase.webhook.validation.WebhookRequestValidator;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.event.model.GithubEventDto;
import za.co.psybergate.chatterbox.domain.event.model.GithubPolledEventDto;
import za.co.psybergate.chatterbox.domain.event.model.WebhookEventDto;
import za.co.psybergate.chatterbox.domain.event.notification.WebhookEventProcessed;
import za.co.psybergate.chatterbox.domain.github.model.GithubRepositoryInformationDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static za.co.psybergate.chatterbox.domain.api.GithubApiJsonKeys.FULL_NAME;

@Service
@RequiredArgsConstructor
@Transactional
public class GithubWebhookServiceImpl implements GithubWebhookService {

    private final WebhookRequestValidator webhookRequestValidator;

    private final GithubEventMapper eventExtractor;

    private final JsonConverter jsonConverter;

    private final GithubPollingService githubPollingService;

    private final WebhookEventStore webhookEventStore;

    private final GithubPolledEventStore githubPolledEventStore;

    private final ApplicationEventPublisher publisher;

    private final WebhookLogger webhookLogger;

    @Override
    public WebhookEventDto process(String eventType, String deliveryId, JsonNode rawBody) {
        String repositoryName = jsonConverter.getRepositoryName(rawBody);
        webhookRequestValidator.assertAcceptedRepository(repositoryName);
        webhookRequestValidator.assertAcceptedEvent(eventType);
        GithubEventDto eventDto = getEventDto(eventType, rawBody);
        WebhookEventDto webhookEvent = webhookEventStore.storeWebhook(deliveryId, eventDto, rawBody);
        publisher.publishEvent(new WebhookEventProcessed());
        return webhookEvent;
    }

    @Override
    public List<GithubPolledEventDto> pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime) {
        return pollGithubForChanges(owner, repositoryName, lastReceivedTime, LocalDateTime.now());
    }

    @Override
    public List<GithubPolledEventDto> pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        webhookRequestValidator.assertAcceptedRepository(owner, repositoryName);
        GithubRepositoryInformationDto recentUpdates = githubPollingService.getRecentUpdates(owner, repositoryName, fromDate, untilDate);
        String repositoryFullName = String.format("%s/%s", owner, repositoryName);
        List<GithubPolledEventDto> updates = new ArrayList<>();
        for (Map.Entry<EventType, ArrayNode> entry : recentUpdates.getGithubEventTypeDetails().entrySet()) {
            ArrayNode arrayNode = entry.getValue();
            EventType eventType = entry.getKey();
            appendToArrayNode(arrayNode, FULL_NAME.getValue(), repositoryFullName);
            List<GithubPolledEventDto> githubPolledEvents = storeEvents(eventType, arrayNode);
            updates.addAll(githubPolledEvents);
        }
        return updates;
    }

    @Override
    public List<GithubPolledEventDto> pollGithubForChanges(String repositoryFullName, LocalDateTime receivedAt) {
        String[] repositoryDetails = repositoryFullName.split("/");
        String owner = repositoryDetails[0];
        String repositoryName = repositoryDetails[1];
        return pollGithubForChanges(owner, repositoryName, receivedAt);
    }

    @Override
    public boolean findMostRecentWebhookAndCheckForUpdatesSince(String repositoryFullName) {
        LocalDateTime lastPersistedTime;
        try {
            WebhookEventDto latestWebhookEvent = webhookEventStore.getMostRecentWebhook(repositoryFullName);
            webhookLogger.logRunnerFoundPreviousWebhook(latestWebhookEvent);
            lastPersistedTime = latestWebhookEvent.receivedAt();
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousWebhooks(repositoryFullName);
            return false;
        }
        try {
            GithubPolledEventDto latestGithubPolledEvent = githubPolledEventStore.getMostRecentPolledEvent(repositoryFullName);
            webhookLogger.logRunnerFoundPreviousPolledEvent(latestGithubPolledEvent);
            lastPersistedTime = getLastPersistedTime(lastPersistedTime, latestGithubPolledEvent.fetchedAt());
        } catch (ApplicationException e) {
            webhookLogger.logRunnerFoundNoPreviousPolledEvents(repositoryFullName);
        }
        List<GithubPolledEventDto> githubPolledEvents = pollGithubForChanges(repositoryFullName, lastPersistedTime);
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

    private List<GithubPolledEventDto> storeEvents(EventType eventType, ArrayNode arrayNode) {
        List<GithubPolledEventDto> updates = new ArrayList<>();
        for (JsonNode jsonNode : arrayNode) {
            String uniqueId = eventType.getUniqueId(jsonNode);
            GithubEventDto eventDto = getEventDto(eventType.name(), jsonNode);
            GithubPolledEventDto polledEvent = githubPolledEventStore.storeEvent(uniqueId, eventDto, jsonNode);
            updates.add(polledEvent);
        }
        return updates;
    }

    private GithubEventDto getEventDto(String eventType, JsonNode rawBody) {
        return eventExtractor.map(eventType, rawBody);
    }

    private LocalDateTime getLastPersistedTime(LocalDateTime persistedTime001, LocalDateTime persistedTime002) {
        if (persistedTime001.isAfter(persistedTime002)) {
            return persistedTime001;
        }
        return persistedTime002;
    }

}
