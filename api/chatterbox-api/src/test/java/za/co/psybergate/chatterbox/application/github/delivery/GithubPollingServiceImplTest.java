package za.co.psybergate.chatterbox.application.github.delivery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSecurityApiGithubProperties;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        GithubPollingServiceImpl.class,
        ApplicationConfig.class,
})
@ActiveProfiles({"test", "live-url"})
public class GithubPollingServiceImplTest {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private ChatterboxSecurityApiGithubProperties apiGithubProperties;

    @Autowired
    private GithubPollingService githubPollingService;

    @Tag("live-integration")
    @DisplayName("Can Query Commits")
    @Test
    public void givenRepositoryDetails_AndStartingDate_WhenQueryCommits_ThenSuccess() throws IOException {
        GitHub gitHub = new GitHubBuilder()
                .withOAuthToken(apiGithubProperties.getToken())
                .build();
        GHRepository repository = gitHub.getRepository("psyAlexBlakeGoudemond/chatterbox");
        LocalDateTime lastReceivedUpdate = LocalDateTime.of(2025, 12, 15, 10, 0);

        List<GHCommit> commitsSince = githubPollingService.getCommitsSince(repository, lastReceivedUpdate, LocalDateTime.now());
        assertNotNull(commitsSince);
        assertFalse(commitsSince.isEmpty());
    }

    @Tag("live-integration")
    @DisplayName("Can Query Pull Requests")
    @Test
    public void givenRepositoryDetails_AndStartingDate_WhenQueryPullRequests_ThenSuccess() throws IOException {
        GitHub gitHub = new GitHubBuilder()
                .withOAuthToken(apiGithubProperties.getToken())
                .build();
        GHRepository repository = gitHub.getRepository("psyAlexBlakeGoudemond/chatterbox");
        LocalDateTime lastReceivedUpdate = LocalDateTime.of(2025, 12, 15, 10, 0);

        List<GHPullRequest> pullRequestsSince = githubPollingService.getPullRequestsSince(repository, lastReceivedUpdate, LocalDateTime.now());
        assertNotNull(pullRequestsSince);
        assertFalse(pullRequestsSince.isEmpty());
    }

}