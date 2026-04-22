package com.webhook.relay.chatterbox.adapter.out.webhook.poll;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.webhook.relay.chatterbox.adapter.out.github.model.GithubApiJsonKeys;
import com.webhook.relay.chatterbox.application.common.exception.ApplicationException;
import com.webhook.relay.chatterbox.application.common.logging.WebhookLogger;
import com.webhook.relay.chatterbox.application.domain.event.model.RawEventPayload;
import com.webhook.relay.chatterbox.application.domain.event.model.RepositoryUpdates;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.port.out.webhook.poll.WebhookPollingPort;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxApiProperties;
import com.webhook.relay.chatterbox.common.config.properties.ChatterboxSourceGithubPayloadProperties;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType.POLL_COMMIT;
import static com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType.POLL_PULL_REQUEST;

@Service("githubRestPollingClient")
public class GithubRestPollingClient implements WebhookPollingPort {

    private final WebClient githubClient;

    private final ChatterboxSourceGithubPayloadProperties payloadProperties;

    private final ChatterboxApiProperties chatterboxApiProperties;

    private final WebhookLogger webhookLogger;

    private final ObjectMapper mapper = new ObjectMapper();

    public GithubRestPollingClient(@Qualifier("githubClient") WebClient webClient,
                                   ChatterboxSourceGithubPayloadProperties payloadProperties,
                                   ChatterboxApiProperties chatterboxApiProperties,
                                   WebhookLogger webhookLogger) {
        this.githubClient = webClient;
        this.payloadProperties = payloadProperties;
        this.webhookLogger = webhookLogger;
        this.chatterboxApiProperties = chatterboxApiProperties;
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
    public List<RawEventPayload> getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate) {
        return getCommitsSince(owner, repositoryName, fromDate, LocalDateTime.now());
    }

    // TODO BlakeGoudemond 2026/01/16 | do we want a retryWhen(...) option?

    /// [API Contract for Commits](https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28)
    @Override
    public List<RawEventPayload> getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
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
        return filterCommitsByDateRange(commits, fromDate, untilDate);
    }

    @Override
    public List<RawEventPayload> getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate) {
        return getPullRequestsSince(owner, repositoryName, fromDate, LocalDateTime.now());
    }

    // TODO BlakeGoudemond 2026/01/16 | do we want a retryWhen(...) option?

    /// [API Contract for Pull Requests](https://docs.github.com/en/rest/pulls/pulls?apiVersion=2022-11-28#list-pull-requests)
    @Override
    public List<RawEventPayload> getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
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
        return filterPullRequestsByDateRange(pullRequests, fromDate, untilDate);
    }

    private List<RawEventPayload> filterPullRequestsByDateRange(JsonNode prArray, LocalDateTime fromDate, LocalDateTime untilDate) {
        int toleranceInSeconds = chatterboxApiProperties.getPolledEventToleranceInSeconds();
        ZoneOffset systemOffset = OffsetDateTime.now().getOffset();
        Instant from = fromDate.plusSeconds(toleranceInSeconds).toInstant(systemOffset);
        Instant until = untilDate.toInstant(systemOffset);
        return getPullRequestsWithinRange(prArray, from, until);
    }

    private List<RawEventPayload> filterCommitsByDateRange(JsonNode commitArray, LocalDateTime fromDate, LocalDateTime untilDate) {
        int toleranceInSeconds = chatterboxApiProperties.getPolledEventToleranceInSeconds();
        ZoneOffset systemOffset = OffsetDateTime.now().getOffset();
        Instant from = fromDate.plusSeconds(toleranceInSeconds).toInstant(systemOffset);
        Instant until = untilDate.toInstant(systemOffset);
        return getCommitsWithinRange(commitArray, from, until);
    }

    private List<RawEventPayload> getPullRequestsWithinRange(JsonNode prArray, Instant from, Instant until) {
        List<RawEventPayload> filtered = new ArrayList<>();
        for (JsonNode pr : prArray) {
            JsonNode mergedAtNode = pr.get(GithubApiJsonKeys.MERGED_AT.getValue());
            if (mergedAtNode == null || mergedAtNode.isNull()) {
                continue;
            }
            Instant mergedAt = Instant.parse(mergedAtNode.asText());
            if (mergedAt.isAfter(from) && mergedAt.isBefore(until)) {
                filtered.add(RawEventPayload.of(pr));
            }
        }
        return filtered;
    }

    private List<RawEventPayload> getCommitsWithinRange(JsonNode commitArray, Instant from, Instant until) {
        List<RawEventPayload> filtered = new ArrayList<>();
        for (JsonNode commit : commitArray) {
            JsonNode commitNode = commit.get("commit");
            if (commitNode == null || commitNode.isNull()) {
                continue;
            }
            JsonNode committerDateNode = commitNode.path("committer").path("date");
            JsonNode authorDateNode = commitNode.path("author").path("date");
            JsonNode commitDateNode = committerDateNode.isMissingNode() ? authorDateNode : committerDateNode;
            if (commitDateNode.isMissingNode() || commitDateNode.isNull()) {
                continue;
            }
            Instant commitDate = Instant.parse(commitDateNode.asText());
            if (commitDate.isAfter(from) && commitDate.isBefore(until)) {
                filtered.add(RawEventPayload.of(commit));
            }
        }
        return filtered;
    }

}
