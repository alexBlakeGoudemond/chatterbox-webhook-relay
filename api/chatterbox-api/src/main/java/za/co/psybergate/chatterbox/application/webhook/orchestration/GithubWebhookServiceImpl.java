package za.co.psybergate.chatterbox.application.webhook.orchestration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.github.delivery.GithubPollingServiceImpl;
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

    private final WebhookReceivedStore  webhookReceivedStore;

    @Override
    public void process(String eventType, String deliveryId, JsonNode rawBody) {
        String repositoryName = jsonConverter.getRepositoryName(rawBody);
        webhookRequestValidator.assertAcceptedRepository(repositoryName);
        webhookRequestValidator.assertAcceptedEvent(eventType);

        HttpResponseDto httpResponseDto = deliverToTeams(eventType, rawBody);
        // TODO BlakeGoudemond 2025/12/28 | persist webhook_received
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
            HttpResponseDto httpResponseDto = deliverToTeams(eventType.getValue(), jsonNode);
            // TODO BlakeGoudemond 2025/12/28 | persist github_polled_item
        }
    }

    @Override
    public HttpResponseDto deliverToTeams(String eventType, JsonNode rawBody) {
        GithubEventDto eventDto = eventExtractor.extract(eventType, rawBody);
        webhookLogger.logWebhookReceived(eventDto);
        webhookLogger.logSendingDtoToTeams(eventDto);
        HttpResponseDto httpResponseDto = teamsSenderService.process(eventDto);
        webhookLogger.logTeamsResponse(httpResponseDto);
        return httpResponseDto;
    }

}
