package za.co.psybergate.chatterbox.application.github.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;
import za.co.psybergate.chatterbox.domain.dto.RepositoryDetail;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles({"test", "live-url"})
class GithubPollingServiceTest {

    @Autowired
    private GithubPollingServiceImpl pollingService;

    @ParameterizedTest(name = "Commits; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollCommits_ThenSuccess(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();
        JsonNode commitsSince = pollingService.getCommitsSince(owner, repositoryName, fromDate, untilDate);
        assertNotNull(commitsSince);
    }

    @ParameterizedTest(name = "Pull Requests; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollPullRequests_ThenSuccess(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();
        JsonNode pullRequestsSince = pollingService.getPullRequestsSince(owner, repositoryName, fromDate, untilDate);
        assertNotNull(pullRequestsSince);
    }

    @ParameterizedTest(name = "Recent Updates; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollRecentUpdates_ThenSuccess(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();
        GithubRepositoryInformationDto recentUpdates = pollingService.getRecentUpdates(owner, repositoryName, fromDate, untilDate);
        assertNotNull(recentUpdates);
        assertNotNull(recentUpdates.commits());
        assertNotNull(recentUpdates.pullRequests());
    }

    private static Stream<Arguments> repositoryDetails() {
        return Stream.of(
                Arguments.of(Named.of("Chatterbox", new RepositoryDetail("psyAlexBlakeGoudemond", "chatterbox", "2025-12-15T06:00:00", "2025-12-16T06:00:00"))),
                Arguments.of(Named.of("SoftwareFoundations", new RepositoryDetail("Psybergate-Knowledge-Repository", "mentoring_software_foundations", "2025-11-26T06:00:00", "2025-11-27T06:00:00")))
        );
    }

}