package za.co.psybergate.chatterbox.application.github.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import za.co.psybergate.chatterbox.application.port.out.github.delivery.GithubPollingPort;
import za.co.psybergate.chatterbox.application.port.out.persistence.GithubPolledEventStorePort;
import za.co.psybergate.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import za.co.psybergate.chatterbox.application.common.logging.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.domain.delivery.RepositoryDetailDto;
import za.co.psybergate.chatterbox.application.domain.event.model.RepositoryUpdates;
import za.co.psybergate.chatterbox.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.adapter.out.github.delivery.GithubRestPollingClient;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        GithubRestPollingClient.class,
        InfrastructurePropertiesConfig.class,
        Slf4jWebhookLogger.class,
})
@ActiveProfiles({"test", "live-url"})
class GithubPollingServiceIT {

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private GithubPollingPort pollingService;

    @MockitoBean
    private WebhookEventStorePort webhookEventStorePort;

    @MockitoBean
    private GithubPolledEventStorePort githubPolledEventStorePort;

    private static Stream<Arguments> repositoryDetails() {
        return Stream.of(
                Arguments.of(Named.of("Chatterbox", new RepositoryDetailDto("psyAlexBlakeGoudemond", "chatterbox", "2025-12-15T06:00:00", "2025-12-16T06:00:00"))),
                Arguments.of(Named.of("SoftwareFoundations", new RepositoryDetailDto("Psybergate-Knowledge-Repository", "mentoring_software_foundations", "2025-11-26T06:00:00", "2025-11-27T06:00:00")))
        );
    }

    @ParameterizedTest(name = "Commits; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollCommits_ThenSuccess(RepositoryDetailDto repositoryDetailDto) {
        String owner = repositoryDetailDto.repositoryOwner();
        String repositoryName = repositoryDetailDto.repositoryName();
        LocalDateTime fromDate = repositoryDetailDto.fromDate();
        LocalDateTime untilDate = repositoryDetailDto.toDate();
        JsonNode commitsSince = pollingService.getCommitsSince(owner, repositoryName, fromDate, untilDate);
        assertNotNull(commitsSince);
    }

    @ParameterizedTest(name = "Pull Requests; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollPullRequests_ThenSuccess(RepositoryDetailDto repositoryDetailDto) {
        String owner = repositoryDetailDto.repositoryOwner();
        String repositoryName = repositoryDetailDto.repositoryName();
        LocalDateTime fromDate = repositoryDetailDto.fromDate();
        LocalDateTime untilDate = repositoryDetailDto.toDate();
        JsonNode pullRequestsSince = pollingService.getPullRequestsSince(owner, repositoryName, fromDate, untilDate);
        assertNotNull(pullRequestsSince);
    }

    @ParameterizedTest(name = "Recent Updates; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollRecentUpdates_ThenSuccess(RepositoryDetailDto repositoryDetailDto) {
        String owner = repositoryDetailDto.repositoryOwner();
        String repositoryName = repositoryDetailDto.repositoryName();
        LocalDateTime fromDate = repositoryDetailDto.fromDate();
        LocalDateTime untilDate = repositoryDetailDto.toDate();
        RepositoryUpdates recentUpdates = pollingService.getRecentUpdates(owner, repositoryName, fromDate, untilDate);
        assertNotNull(recentUpdates);
        assertNotNull(recentUpdates.getWebhookEventTypeDetails());
        assertFalse(recentUpdates.getWebhookEventTypeDetails().isEmpty());
    }

}