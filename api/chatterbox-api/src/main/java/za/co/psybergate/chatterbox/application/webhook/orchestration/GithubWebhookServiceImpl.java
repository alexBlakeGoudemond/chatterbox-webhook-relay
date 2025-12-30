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
import za.co.psybergate.chatterbox.application.teams.delivery.TeamsSenderServiceImpl;
import za.co.psybergate.chatterbox.application.webhook.ingest.WebhookRequestValidator;
import za.co.psybergate.chatterbox.application.webhook.processing.GithubEventExtractorImpl;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;
import za.co.psybergate.chatterbox.domain.dto.HttpResponseDto;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.serialisation.JsonConverter;

import java.time.LocalDateTime;
import java.util.Map;

import static za.co.psybergate.chatterbox.domain.api.GithubApiJsonKeys.FULL_NAME;

@Service
@RequiredArgsConstructor
public class GithubWebhookServiceImpl implements GithubWebhookService {

    private final WebhookRequestValidator webhookRequestValidator;

    private final GithubEventExtractorImpl eventExtractor;

    private final WebhookLogger webhookLogger;

    private final TeamsSenderServiceImpl teamsSenderService;

    private final JsonConverter jsonConverter;

    private final GithubPollingServiceImpl githubPollingService;

    private final WebhookReceivedStore webhookReceivedStore;

    private final GithubPolledStore githubPolledStore;

    @Override
    public void process(String eventType, String deliveryId, JsonNode rawBody) {
        String repositoryName = jsonConverter.getRepositoryName(rawBody);
        webhookRequestValidator.assertAcceptedRepository(repositoryName);
        webhookRequestValidator.assertAcceptedEvent(eventType);
        HttpResponseDto httpResponseDto = deliverToTeams(EventType.get(eventType), deliveryId, rawBody);
    }


    @Override
    public void pollGithubForChanges(String owner, String repositoryName, LocalDateTime lastReceivedTime) {
        pollGithubForChanges(owner, repositoryName, lastReceivedTime, LocalDateTime.now());
    }

    @Override
    public void pollGithubForChanges(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        webhookRequestValidator.assertAcceptedRepository(owner, repositoryName);
        GithubRepositoryInformationDto recentUpdates = githubPollingService.getRecentUpdates(owner, repositoryName, fromDate, untilDate);
        String repositoryFullName = String.format("%s/%s", owner, repositoryName);
        for (Map.Entry<EventType, ArrayNode> entry : recentUpdates.getGithubEventTypeDetails().entrySet()) {
            ArrayNode arrayNode = entry.getValue();
            EventType eventType = entry.getKey();
            appendToArrayNode(arrayNode, FULL_NAME.getValue(), repositoryFullName);
            deliverAllToTeams(eventType, arrayNode);
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

    private void deliverAllToTeams(EventType eventType, ArrayNode arrayNode) {
        for (JsonNode jsonNode : arrayNode) {
            String uniqueId = getUniqueId(eventType, jsonNode);
            HttpResponseDto httpResponseDto = deliverToTeams(eventType, uniqueId, jsonNode);
        }
    }

    // TODO BlakeGoudemond 2025/12/30 | in time, persist here and send to teams in separate class
    @Override
    public HttpResponseDto deliverToTeams(EventType eventType, String uniqueId, JsonNode rawBody) {
        GithubEventDto eventDto = eventExtractor.extract(eventType, rawBody);
        webhookLogger.logWebhookReceived(eventDto);
        webhookLogger.logSendingDtoToTeams(eventDto);
        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto);
        webhookLogger.logTeamsResponse(httpResponseDto);
        // TODO BlakeGoudemond 2025/12/28 | test that this works
//        webhookReceivedStore.storeWebhook(uniqueId, eventDto, rawBody);
        return httpResponseDto;
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
