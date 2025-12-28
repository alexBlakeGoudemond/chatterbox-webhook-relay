package za.co.psybergate.chatterbox.application.github.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.domain.api.GithubApiEventType;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static za.co.psybergate.chatterbox.domain.api.GithubApiEventType.POLL_COMMIT;
import static za.co.psybergate.chatterbox.domain.api.GithubApiEventType.POLL_PULL_REQUEST;

@Service
public class GithubPollingServiceImpl implements GithubPollingService {

    private final WebClient githubClient;

    private final ChatterboxSourceGithubPayloadProperties payloadProperties;

    private final WebhookLogger webhookLogger;

    private final ObjectMapper mapper = new ObjectMapper();

    public GithubPollingServiceImpl(@Qualifier("githubClient") WebClient webClient,
                                    ChatterboxSourceGithubPayloadProperties payloadProperties,
                                    WebhookLogger webhookLogger) {
        this.githubClient = webClient;
        this.payloadProperties = payloadProperties;
        this.webhookLogger = webhookLogger;
    }

    @Override
    public GithubRepositoryInformationDto getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate) {
        return getRecentUpdates(owner, repositoryName, fromDate, LocalDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public @Valid GithubRepositoryInformationDto getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate){
        webhookLogger.logGithubPollRecentUpdates(owner, repositoryName, fromDate, untilDate);
        GithubRepositoryInformationDto informationDto = new GithubRepositoryInformationDto(fromDate, untilDate);
        for (String eventMapping : payloadProperties.getEventMapping().keySet()) {
            boolean eventExists = GithubApiEventType.contains(eventMapping);
            if (!eventExists) {
                continue;
            }
            GithubApiEventType eventType = GithubApiEventType.get(eventMapping);
            switch (eventType) {
                case POLL_COMMIT:
                    informationDto.add(POLL_COMMIT, getCommitsSince(owner, repositoryName, fromDate, untilDate));
                    break;
                case POLL_PULL_REQUEST:
                    informationDto.add(POLL_PULL_REQUEST, getPullRequestsSince(owner, repositoryName, fromDate, untilDate));
                default:
                    break;
            }
        }
        return informationDto;
    }

    @Override
    public ArrayNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate) {
        return getCommitsSince(owner, repositoryName, fromDate, LocalDateTime.now());
    }

    @Override
    public ArrayNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        webhookLogger.logGithubPollEventType("commits", owner, repositoryName, fromDate, untilDate);
        ArrayNode commits = githubClient.get()
                .uri(uri -> uri
                        .path("/repos/{owner}/{repo}/commits")
                        .queryParam("since", fromDate.toString())
                        .queryParam("until", untilDate.toString())
                        .build(owner, repositoryName))
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .block();
        if (commits == null) {
            throw new ApplicationException("No commits found when polling Repository");
        }
        return commits;
    }

    @Override
    public ArrayNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate) {
        return getPullRequestsSince(owner, repositoryName, fromDate, LocalDateTime.now());
    }

    @Override
    public ArrayNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        webhookLogger.logGithubPollEventType("pull_requests", owner, repositoryName, fromDate, untilDate);
        ArrayNode pullRequests = githubClient.get()
                .uri(uri -> uri
                        .path("/repos/{owner}/{repo}/pulls")
                        .queryParam("state", "all")
                        .queryParam("sort", "created")
                        .queryParam("direction", "desc")
                        .queryParam("per_page", 20)
                        .build(owner, repositoryName)
                )
                .retrieve()
                .bodyToMono(ArrayNode.class)
                .block();
        if (pullRequests == null) {
            throw new ApplicationException("No pull requests found when polling Repository");
        }
        return filterByDateRange(pullRequests, fromDate, untilDate);
    }

    private ArrayNode filterByDateRange(JsonNode prArray, LocalDateTime fromDate, LocalDateTime untilDate) {
        ArrayNode filtered = mapper.createArrayNode();
        Instant from = fromDate.toInstant(ZoneOffset.UTC);
        Instant until = untilDate.toInstant(ZoneOffset.UTC);
        for (JsonNode pr : prArray) {
            JsonNode mergedAtNode = pr.get("merged_at");
            if (mergedAtNode == null || mergedAtNode.isNull()) {
                continue;
            }
            Instant mergedAt = Instant.parse(mergedAtNode.asText());
            if (!mergedAt.isBefore(from) && !mergedAt.isAfter(until)) {
                filtered.add(pr);
            }
        }
        return filtered;
    }

}
