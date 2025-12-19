package za.co.psybergate.chatterbox.application.github.delivery;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSecurityApiGithubProperties;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Predicate;

// TODO BlakeGoudemond 2025/12/19 | eventually hook up to a cron job or job that runs on startup
@Service
@RequiredArgsConstructor
public class GithubPollingServiceImpl implements GithubPollingService {

    private final ChatterboxSecurityApiGithubProperties apiGithubProperties;

    public void doSomeWork() throws IOException {
        GitHub gitHub = new GitHubBuilder()
                .withOAuthToken(apiGithubProperties.getToken())
                .build();
        GHRepository repository = gitHub.getRepository("psyAlexBlakeGoudemond/chatterbox");
        LocalDateTime lastReceivedUpdate = LocalDateTime.of(2025, 12, 19, 10, 0);

        List<GHPullRequest> pullRequestsSince = getPullRequestsSince(repository, lastReceivedUpdate);
        List<GHCommit> commitsSince = getCommitsSince(repository, lastReceivedUpdate);
        System.out.println("pullRequestsSince = " + pullRequestsSince);
        System.out.println("commitsSince = " + commitsSince);
    }

    @Override
    public List<GHCommit> getCommitsSince(GHRepository repository, LocalDateTime lastReceivedUpdate) {
        return getCommitsSince(repository, lastReceivedUpdate, LocalDateTime.now());
    }

    // TODO BlakeGoudemond 2025/12/19 | log where helpful
    @Override
    public List<GHCommit> getCommitsSince(GHRepository repository, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return repository.queryCommits()
                    .since(startDate.toEpochSecond(ZoneOffset.UTC))
                    .list()
                    .toList()
                    .stream()
                    .filter(commitIsBetween(startDate, endDate))
                    .toList();
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
        try {
            return repository.queryPullRequests()
                    .state(GHIssueState.ALL)
                    .list()
                    .toList()
                    .stream()
                    .filter(pullRequestIsBetween(startDate, endDate))
                    .toList();
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
