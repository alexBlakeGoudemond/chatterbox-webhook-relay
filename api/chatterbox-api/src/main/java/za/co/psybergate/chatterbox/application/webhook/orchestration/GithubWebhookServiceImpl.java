package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.github.delivery.GithubPollingServiceImpl;
import za.co.psybergate.chatterbox.application.persistence.GithubPolledStore;
import za.co.psybergate.chatterbox.application.persistence.WebhookReceivedStore;
import za.co.psybergate.chatterbox.application.webhook.ingest.WebhookRequestValidator;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverter;

import java.time.LocalDateTime;
import java.util.Map;

import static za.co.psybergate.chatterbox.domain.api.GithubApiJsonKeys.FULL_NAME;

@Service
@RequiredArgsConstructor
public class GithubWebhookServiceImpl implements GithubWebhookService {

    private final WebhookRequestValidator webhookRequestValidator;

    private final GithubEventExtractorImpl eventExtractor;
    
    private final JsonConverter jsonConverter;

    private final GithubPollingServiceImpl githubPollingService;

    private final WebhookReceivedStore webhookReceivedStore;

    private final GithubPolledStore githubPolledStore;

    // TODO BlakeGoudemond 2026/01/01 | create cron job to check for updates - then deliver
    // TODO BlakeGoudemond 2026/01/01 | persist now
    @Override
    public void process(String eventType, String deliveryId, JsonNode rawBody) {
        String repositoryName = jsonConverter.getRepositoryName(rawBody);
        // TODO BlakeGoudemond 2026/01/01 | part of filter?
        webhookRequestValidator.assertAcceptedRepository(repositoryName);
        webhookRequestValidator.assertAcceptedEvent(eventType);
        GithubEventDto eventDto = getEventDto(eventType, rawBody);
        webhookReceivedStore.storeWebhook(deliveryId, eventDto, rawBody);
    }

    @Override
    public void pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime) {
        pollGithubForChanges(owner, repositoryName, lastReceivedTime, LocalDateTime.now());
    }

    @Override
    public void pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        // TODO BlakeGoudemond 2026/01/01 | Does the filter fire for this method? Its not wired to controller
        webhookRequestValidator.assertAcceptedRepository(owner, repositoryName);
        GithubRepositoryInformationDto recentUpdates = githubPollingService.getRecentUpdates(owner, repositoryName, fromDate, untilDate);
        String repositoryFullName = String.format("%s/%s", owner, repositoryName);
        for (Map.Entry<EventType, ArrayNode> entry : recentUpdates.getGithubEventTypeDetails().entrySet()) {
            ArrayNode arrayNode = entry.getValue();
            EventType eventType = entry.getKey();
            appendToArrayNode(arrayNode, FULL_NAME.getValue(), repositoryFullName);
            storeEvents(eventType, arrayNode);
        }
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

    private void storeEvents(EventType eventType, ArrayNode arrayNode) {
        for (JsonNode jsonNode : arrayNode) {
            String uniqueId = getUniqueId(eventType, jsonNode);
            GithubEventDto eventDto = getEventDto(eventType.name(), jsonNode);
            githubPolledStore.storeEvent(uniqueId, eventDto, jsonNode);
        }
    }

    private GithubEventDto getEventDto(String eventType, JsonNode rawBody) {
        return eventExtractor.extract(eventType, rawBody);
    }

    private String getUniqueId(EventType eventType, JsonNode jsonNode) {
        String uniqueId;
        switch (eventType) {
            case POLL_COMMIT:
                uniqueId = jsonNode.get("sha").asText();
                break;
            case POLL_PULL_REQUEST:
                uniqueId = jsonNode.get("merge_commit_sha").asText();
                break;
            default:
                throw new ApplicationException("Unable to find UniqueID; Unknown event type " + eventType);
        }
        return uniqueId;
    }

}
