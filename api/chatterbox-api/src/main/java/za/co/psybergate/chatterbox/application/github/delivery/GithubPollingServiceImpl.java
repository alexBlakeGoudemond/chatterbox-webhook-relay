package za.co.psybergate.chatterbox.application.github.delivery;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSecurityApiGithubProperties;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Predicate;

// TODO BlakeGoudemond 2025/12/19 | eventually hook up to a cron job or job that runs on startup
@Service
@RequiredArgsConstructor
public class GithubPollingServiceImpl implements GithubPollingService {

    private final WebhookLogger  webhookLogger;

    private final ChatterboxSecurityApiGithubProperties apiGithubProperties;

    // TODO BlakeGoudemond 2025/12/20 | method to loop through each accepted repo and check if any updates since <xDate>
    public void doSomeWork() throws IOException {
        GHRepository repository = getGithubRepository("psyAlexBlakeGoudemond/chatterbox");
        LocalDateTime lastReceivedUpdate = LocalDateTime.of(2025, 12, 19, 10, 0);

        List<GHPullRequest> pullRequestsSince = getPullRequestsSince(repository, lastReceivedUpdate);
        List<GHCommit> commitsSince = getCommitsSince(repository, lastReceivedUpdate);
        System.out.println("pullRequestsSince = " + pullRequestsSince);
        System.out.println("commitsSince = " + commitsSince);
    }

    @Override
    public GHRepository getGithubRepository(String repositoryFullName) {
        GitHub gitHub = getGithubApiHandle();
        try {
            return gitHub.getRepository(repositoryFullName);
        } catch (IOException e) {
            throw new ApplicationException("Unexpected issue when creating Repository from name", e);
        }
    }

    private GitHub getGithubApiHandle() {
        try {
            return new GitHubBuilder()
                    .withOAuthToken(apiGithubProperties.getToken())
                    .build();
        } catch (IOException e) {
            throw new ApplicationException("Unexpected issue when creating Github API handle", e);
        }
    }

    @Override
    public List<GHCommit> getCommitsSince(GHRepository repository, LocalDateTime lastReceivedUpdate) {
        return getCommitsSince(repository, lastReceivedUpdate, LocalDateTime.now());
    }

    @Override
    public List<GHCommit> getCommitsSince(GHRepository repository, LocalDateTime startDate, LocalDateTime endDate) {
        webhookLogger.logQueryingGithubApi("commits", repository, startDate, endDate);
        try {
            List<GHCommit> commits = repository.queryCommits()
                    .since(startDate.toEpochSecond(ZoneOffset.UTC))
                    .list()
                    .toList()
                    .stream()
                    .filter(commitIsBetween(startDate, endDate))
                    .toList();
            webhookLogger.logQueryingGithubApiCompleted("commits", commits);
            return commits;
        } catch (IOException e) {
            throw new ApplicationException("Unexpected issue when retrieving Commits", e);
        }
    }

    @Override
    public List<GHPullRequest> getPullRequestsSince(GHRepository repository, LocalDateTime lastReceivedUpdate) {
        return getPullRequestsSince(repository, lastReceivedUpdate, LocalDateTime.now());
    }

    @Override
    public List<GHPullRequest> getPullRequestsSince(GHRepository repository, LocalDateTime startDate, LocalDateTime endDate) {
        webhookLogger.logQueryingGithubApi("pullRequests", repository, startDate, endDate);
        try {
            List<GHPullRequest> pullRequests = repository.queryPullRequests()
                    .state(GHIssueState.ALL)
                    .list()
                    .toList()
                    .stream()
                    .filter(pullRequestIsBetween(startDate, endDate))
                    .toList();
            webhookLogger.logQueryingGithubApiCompleted("pullRequests", pullRequests);
            return pullRequests;
        } catch (IOException e) {
            throw new ApplicationException("Unexpected issue when retrieving PullRequests", e);
        }
    }

    private Predicate<GHCommit> commitIsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        Instant startDateAsInstant = startDate.atZone(ZoneOffset.UTC).toInstant();
        Instant endDateAsInstant = endDate.atZone(ZoneOffset.UTC).toInstant();
        return commit -> {
            try {
                Instant commitAsInstant = commit.getCommitDate().toInstant();
                return commitAsInstant.isAfter(startDateAsInstant) && commitAsInstant.isBefore(endDateAsInstant);
            } catch (IOException e) {
                throw new ApplicationException("Unexpected issue when retrieving CommitDate", e);
            }
        };
    }

    private Predicate<GHPullRequest> pullRequestIsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        Instant startDateAsInstant = startDate.atZone(ZoneOffset.UTC).toInstant();
        Instant endDateAsInstant = endDate.atZone(ZoneOffset.UTC).toInstant();
        return pullRequest -> {
            try {
                Instant pullRequestAsInstant = pullRequest.getUpdatedAt().toInstant();
                return pullRequestAsInstant.isAfter(startDateAsInstant) && pullRequestAsInstant.isBefore(endDateAsInstant);
            } catch (IOException e) {
                throw new ApplicationException("Unexpected issue when retrieving UpdatedAt", e);
            }
        };
    }

}
