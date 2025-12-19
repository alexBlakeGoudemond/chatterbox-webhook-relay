package za.co.psybergate.chatterbox.application.github.delivery;

import org.junit.jupiter.api.*;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
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

    private GitHub githubApi;

    @BeforeEach
    public void setup() {
        try {
            this.githubApi = new GitHubBuilder()
                    .withOAuthToken(apiGithubProperties.getToken())
                    .build();
        } catch (IOException e) {
            Assertions.fail("Unable to create the base Github POJO", e);
        }
    }

    @Tag("live-integration")
    @DisplayName("Can Query Commits")
    @Test
    public void givenRepositoryDetails_AndStartingDate_WhenQueryCommits_ThenSuccess() {
        GHRepository repository = getGithubRepository();
        LocalDateTime lastReceivedUpdate = LocalDateTime.of(2025, 12, 15, 10, 0);

        List<GHCommit> commitsSince = githubPollingService.getCommitsSince(repository, lastReceivedUpdate, LocalDateTime.now());
        assertNotNull(commitsSince);
        assertFalse(commitsSince.isEmpty());
    }

    @Tag("live-integration")
    @DisplayName("Can Query Pull Requests")
    @Test
    public void givenRepositoryDetails_AndStartingDate_WhenQueryPullRequests_ThenSuccess() throws IOException {
        GHRepository repository = getGithubRepository();
        LocalDateTime lastReceivedUpdate = LocalDateTime.of(2025, 12, 15, 10, 0);

        List<GHPullRequest> pullRequestsSince = githubPollingService.getPullRequestsSince(repository, lastReceivedUpdate, LocalDateTime.now());
        assertNotNull(pullRequestsSince);
        assertFalse(pullRequestsSince.isEmpty());
    }

    private GHRepository getGithubRepository() {
        try {
            return githubApi.getRepository("psyAlexBlakeGoudemond/chatterbox");
        } catch (IOException e) {
            throw new ApplicationException("Unable to retrieve repository", e);
        }
    }

}