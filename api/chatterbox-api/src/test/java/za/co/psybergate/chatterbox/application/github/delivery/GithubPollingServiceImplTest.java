package za.co.psybergate.chatterbox.application.github.delivery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        GithubPollingServiceImpl.class,
})
@ActiveProfiles({"test", "live-url"})
public class GithubPollingServiceImplTest {

    @Autowired
    private GithubPollingService githubPollingService;

    // TODO BlakeGoudemond 2025/12/19 | place auth token in properties
    @Tag("live-integration")
    @DisplayName("Can Query Commits")
    @Test
    public void givenRepositoryDetails_AndStartingDate_WhenQueryCommits_ThenSuccess() throws IOException {
        GitHub gitHub = new GitHubBuilder()
                .withOAuthToken("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")
                .build();
        GHRepository repository = gitHub.getRepository("psyAlexBlakeGoudemond/chatterbox");
        LocalDateTime lastReceivedUpdate = LocalDateTime.of(2025, 12, 15, 10, 0);

        List<GHCommit> commitsSince = githubPollingService.getCommitsSince(repository, lastReceivedUpdate);
        assertNotNull(commitsSince);
        assertFalse(commitsSince.isEmpty());
    }

    @Tag("live-integration")
    @DisplayName("Can Query Pull Requests")
    @Test
    public void givenRepositoryDetails_AndStartingDate_WhenQueryPullRequests_ThenSuccess() throws IOException {
        GitHub gitHub = new GitHubBuilder()
                .withOAuthToken("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")
                .build();
        GHRepository repository = gitHub.getRepository("psyAlexBlakeGoudemond/chatterbox");
        LocalDateTime lastReceivedUpdate = LocalDateTime.of(2025, 12, 15, 10, 0);

        List<GHPullRequest> pullRequestsSince = githubPollingService.getPullRequestsSince(repository, lastReceivedUpdate);
        assertNotNull(pullRequestsSince);
        assertFalse(pullRequestsSince.isEmpty());
    }

}