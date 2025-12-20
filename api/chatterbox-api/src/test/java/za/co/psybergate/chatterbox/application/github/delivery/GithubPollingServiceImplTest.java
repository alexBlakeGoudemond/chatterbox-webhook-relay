package za.co.psybergate.chatterbox.application.github.delivery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSecurityApiGithubProperties;
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLogger;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        GithubPollingServiceImpl.class,
        ApplicationConfig.class,
        WebhookLogger.class,
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
    @ParameterizedTest(name = "Commits; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetails_AndStartingDate_WhenQueryCommits_ThenSuccess(RepositoryDetail repositoryDetail) {
        GHRepository repository = getGithubRepository(repositoryDetail.repositoryFullName());
        LocalDateTime lastReceivedUpdate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();

        List<GHCommit> commitsSince = githubPollingService.getCommitsSince(repository, lastReceivedUpdate, untilDate);
        assertNotNull(commitsSince);
        assertFalse(commitsSince.isEmpty());
    }

    @Tag("live-integration")
    @ParameterizedTest(name = "PullRequests; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetails_AndStartingDate_WhenQueryPullRequests_ThenSuccess(RepositoryDetail repositoryDetail) {
        GHRepository repository = getGithubRepository(repositoryDetail.repositoryFullName());
        LocalDateTime lastReceivedUpdate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();

        List<GHPullRequest> pullRequestsSince = githubPollingService.getPullRequestsSince(repository, lastReceivedUpdate, untilDate);
        assertNotNull(pullRequestsSince);
        assertFalse(pullRequestsSince.isEmpty());
    }

    private static Stream<Arguments> repositoryDetails() {
        return Stream.of(
                Arguments.of(Named.of("Chatterbox", new RepositoryDetail("psyAlexBlakeGoudemond/chatterbox", "2025-12-15T06:00:00", "2025-12-16T06:00:00"))),
                Arguments.of(Named.of("SoftwareFoundations", new RepositoryDetail("Psybergate-Knowledge-Repository/mentoring_software_foundations", "2025-11-26T06:00:00", "2025-11-27T06:00:00")))
        );
    }

    private GHRepository getGithubRepository(String repoName) {
        try {
            return githubApi.getRepository(repoName);
        } catch (IOException e) {
            throw new ApplicationException("Unable to retrieve repository", e);
        }
    }

    public record RepositoryDetail(String repositoryFullName, LocalDateTime fromDate, LocalDateTime toDate) {

        private RepositoryDetail(String repositoryFullName, String fromDate, String toDate) {
            this(repositoryFullName, LocalDateTime.parse(fromDate), LocalDateTime.parse(toDate));
        }

    }

}