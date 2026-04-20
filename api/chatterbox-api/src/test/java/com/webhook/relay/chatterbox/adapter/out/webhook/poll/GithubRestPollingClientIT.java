package com.webhook.relay.chatterbox.adapter.out.webhook.poll;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.webhook.relay.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import com.webhook.relay.chatterbox.adapter.in.web.filter.WebhookFilter;
import com.webhook.relay.chatterbox.application.common.logging.slf4j.Slf4jWebhookLogger;
import com.webhook.relay.chatterbox.application.domain.delivery.RepositoryDetail;
import com.webhook.relay.chatterbox.application.domain.event.model.RepositoryUpdates;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookEventStorePort;
import com.webhook.relay.chatterbox.application.port.out.persistence.WebhookPolledEventStorePort;
import com.webhook.relay.chatterbox.common.config.InfrastructurePropertiesConfig;
import com.webhook.relay.chatterbox.common.logging.convenience.ImportSlf4jWebhookLogger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ImportSlf4jWebhookLogger
@SpringBootTest(classes = {
        GithubRestPollingClient.class,
        InfrastructurePropertiesConfig.class,
        Slf4jWebhookLogger.class,
})
@ActiveProfiles({"test", "live-url"})
class GithubRestPollingClientIT {

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @MockitoBean
    private WebhookFilter webhookFilter;

    @Autowired
    private GithubRestPollingClient githubRestPollingClient;

    @MockitoBean
    private WebhookEventStorePort webhookEventStorePort;

    @MockitoBean
    private WebhookPolledEventStorePort webhookPolledEventStorePort;

    private static Stream<Arguments> repositoryDetails() {
        return Stream.of(
                Arguments.of(Named.of("Chatterbox", new RepositoryDetail("alexBlakeGoudemond", "chatterbox", "2025-12-15T06:00:00", "2025-12-16T06:00:00"))),
                Arguments.of(Named.of("SoftwareFoundations", new RepositoryDetail("Company-Knowledge-Repository", "mentoring_software_foundations", "2025-11-26T06:00:00", "2025-11-27T06:00:00")))
        );
    }

    @ParameterizedTest(name = "Commits; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollCommits_ThenSuccess(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();
        List<?> commitsSince = githubRestPollingClient.getCommitsSince(owner, repositoryName, fromDate, untilDate);
        assertNotNull(commitsSince);
    }

    @ParameterizedTest(name = "Pull Requests; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollPullRequests_ThenSuccess(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();
        List<?> pullRequestsSince = githubRestPollingClient.getPullRequestsSince(owner, repositoryName, fromDate, untilDate);
        assertNotNull(pullRequestsSince);
    }

    @ParameterizedTest(name = "Recent Updates; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void givenRepositoryDetailsAndDates_WhenPollRecentUpdates_ThenSuccess(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();
        RepositoryUpdates recentUpdates = githubRestPollingClient.getRecentUpdates(owner, repositoryName, fromDate, untilDate);
        assertNotNull(recentUpdates);
        assertNotNull(recentUpdates.getWebhookEventTypeDetails());
        assertFalse(recentUpdates.getWebhookEventTypeDetails().isEmpty());
    }

}