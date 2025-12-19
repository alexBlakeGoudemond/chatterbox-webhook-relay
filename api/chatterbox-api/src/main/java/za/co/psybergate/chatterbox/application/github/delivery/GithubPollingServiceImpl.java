package za.co.psybergate.chatterbox.application.github.delivery;

import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Predicate;

// TODO BlakeGoudemond 2025/12/19 | eventually hook up to a cron job or job that runs on startup
@Service
public class GithubPollingServiceImpl implements GithubPollingService {

    public void doSomeWork() throws IOException {
        GitHub gitHub = new GitHubBuilder()
                .withOAuthToken("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")
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
        try {
            return repository.queryCommits()
                    .since(lastReceivedUpdate.toEpochSecond(ZoneOffset.UTC))
                    .list()
                    .toList();
        } catch (IOException e) {
            throw new ApplicationException("Unexpected issue when retrieving Commits", e);
        }
    }

    @Override
    public List<GHPullRequest> getPullRequestsSince(GHRepository repository, LocalDateTime lastReceivedUpdate) {
        try {
            return repository.queryPullRequests()
                    .state(GHIssueState.ALL)
                    .list()
                    .toList()
                    .stream()
                    .filter(isAfter(lastReceivedUpdate))
                    .toList();
        } catch (IOException e) {
            throw new ApplicationException("Unexpected issue when retrieving PullRequests", e);
        }
    }

    private Predicate<GHPullRequest> isAfter(LocalDateTime lastReceivedUpdate) {
        ZoneId zoneId = ZoneId.of("Z");

        Instant instant = lastReceivedUpdate.atZone(zoneId).toInstant();
        return pr -> {
            try {
                return pr.getUpdatedAt().toInstant().isAfter(instant);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
