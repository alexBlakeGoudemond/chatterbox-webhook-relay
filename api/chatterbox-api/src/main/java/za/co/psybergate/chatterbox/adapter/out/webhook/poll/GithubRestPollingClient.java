package za.co.psybergate.chatterbox.adapter.out.webhook.poll;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import za.co.psybergate.chatterbox.application.port.out.webhook.poll.WebhookPollingPort;
import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.common.logging.WebhookLogger;
import za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType;
import za.co.psybergate.chatterbox.application.domain.event.model.RepositoryUpdates;
import za.co.psybergate.chatterbox.common.config.properties.ChatterboxSourceGithubPayloadProperties;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType.POLL_COMMIT;
import static za.co.psybergate.chatterbox.application.domain.event.model.WebhookEventType.POLL_PULL_REQUEST;

@Service
public class GithubRestPollingClient implements WebhookPollingPort {

    private final WebClient githubClient;

    private final ChatterboxSourceGithubPayloadProperties payloadProperties;

    private final WebhookLogger webhookLogger;

    private final ObjectMapper mapper = new ObjectMapper();

    public GithubRestPollingClient(@Qualifier("githubClient") WebClient webClient,
                                   ChatterboxSourceGithubPayloadProperties payloadProperties,
                                   WebhookLogger webhookLogger) {
        this.githubClient = webClient;
        this.payloadProperties = payloadProperties;
        this.webhookLogger = webhookLogger;
    }

    @Override
    public RepositoryUpdates getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate) {
        return getRecentUpdates(owner, repositoryName, fromDate, LocalDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public @Valid RepositoryUpdates getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        webhookLogger.logPollRecentUpdates(owner, repositoryName, fromDate, untilDate);
        RepositoryUpdates informationDto = new RepositoryUpdates(fromDate, untilDate);
        for (String eventMapping : payloadProperties.getEventMapping().keySet()) {
            boolean eventExists = WebhookEventType.contains(eventMapping);
            if (!eventExists) {
                continue;
            }
            WebhookEventType webhookEventType = WebhookEventType.get(eventMapping);
            switch (webhookEventType) {
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
    public List<?> getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate) {
        return getCommitsSince(owner, repositoryName, fromDate, LocalDateTime.now());
    }

    // TODO BlakeGoudemond 2026/01/16 | do we want a retryWhen(...) option?
    /// [API Contract for Commits](https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28)
    @Override
    public List<?> getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        webhookLogger.logPollEventType("commits", owner, repositoryName, fromDate, untilDate);
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
        List<JsonNode> list = new ArrayList<>();
        commits.forEach(list::add);
        return list;
    }

    @Override
    public List<?> getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate) {
        return getPullRequestsSince(owner, repositoryName, fromDate, LocalDateTime.now());
    }

    // TODO BlakeGoudemond 2026/01/16 | do we want a retryWhen(...) option?
    /// [API Contract for Pull Requests](https://docs.github.com/en/rest/pulls/pulls?apiVersion=2022-11-28#list-pull-requests)
    @Override
    public List<?> getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        webhookLogger.logPollEventType("pull_requests", owner, repositoryName, fromDate, untilDate);
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

    private List<?> filterByDateRange(JsonNode prArray, LocalDateTime fromDate, LocalDateTime untilDate) {
        List<JsonNode> filtered = new ArrayList<>();
        ZoneOffset systemOffset = OffsetDateTime.now().getOffset();
        Instant from = fromDate.toInstant(systemOffset);
        Instant until = untilDate.toInstant(systemOffset);
        for (JsonNode pr : prArray) {
            JsonNode mergedAtNode = pr.get("merged_at");
            if (mergedAtNode == null || mergedAtNode.isNull()) {
                continue;
            }
            Instant mergedAt = Instant.parse(mergedAtNode.asText());
            if (mergedAt.isAfter(from) && mergedAt.isBefore(until)) {
                filtered.add(pr);
            }
        }
        return filtered;
    }

}
