package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.github.delivery.GithubPollingService;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.serialisation.JsonConverter;
import za.co.psybergate.chatterbox.application.webhook.ingest.WebhookRequestValidator;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractor;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;
import za.co.psybergate.chatterbox.domain.event.WebhookEventProcessed;
import za.co.psybergate.chatterbox.domain.persistence.dto.GithubPolledEventDto;
import za.co.psybergate.chatterbox.domain.persistence.dto.WebhookEventDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static za.co.psybergate.chatterbox.domain.api.GithubApiJsonKeys.FULL_NAME;

@Service
@RequiredArgsConstructor
public class GithubWebhookServiceImpl implements GithubWebhookService {

    private final WebhookRequestValidator webhookRequestValidator;

    private final GithubEventExtractor eventExtractor;

    private final JsonConverter jsonConverter;

    private final GithubPollingService githubPollingService;

    private final WebhookReceivedStore webhookReceivedStore;

    private final GithubPolledStore githubPolledStore;

    private final ApplicationEventPublisher publisher;

    @Override
    public WebhookEventDto process(String eventType, String deliveryId, JsonNode rawBody) {
        String repositoryName = jsonConverter.getRepositoryName(rawBody);
        webhookRequestValidator.assertAcceptedRepository(repositoryName);
        webhookRequestValidator.assertAcceptedEvent(eventType);
        GithubEventDto eventDto = getEventDto(eventType, rawBody);
        WebhookEventDto webhookEvent = webhookReceivedStore.storeWebhook(deliveryId, eventDto, rawBody);
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
            String uniqueId = getUniqueId(eventType, jsonNode);
            GithubEventDto eventDto = getEventDto(eventType.name(), jsonNode);
            GithubPolledEventDto polledEvent = githubPolledStore.storeEvent(uniqueId, eventDto, jsonNode);
            updates.add(polledEvent);
        }
        return updates;
    }

    private GithubEventDto getEventDto(String eventType, JsonNode rawBody) {
        return eventExtractor.extract(eventType, rawBody);
    }

    private String getUniqueId(EventType eventType, JsonNode jsonNode) {
        return switch (eventType) {
            case POLL_COMMIT -> jsonNode.get("sha").asText();
            case POLL_PULL_REQUEST -> jsonNode.get("merge_commit_sha").asText();
            default -> throw new ApplicationException("Unable to find UniqueID; Unknown event type " + eventType);
        };
    }

}
