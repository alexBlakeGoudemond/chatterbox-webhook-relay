package com.webhook.relay.chatterbox.application.usecase.webhook.orchestration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.webhook.relay.architecture_rules.quality.MirrorProductionClassForArchitectureRuleTests;
import com.webhook.relay.chatterbox.adapter.in.actuator.WebhookRuntimeMetrics;
import com.webhook.relay.chatterbox.adapter.in.validation.GithubWebhookValidator;
import com.webhook.relay.chatterbox.adapter.in.web.filter.WebhookFilter;
import com.webhook.relay.chatterbox.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import com.webhook.relay.chatterbox.adapter.out.persistence.WebhookPolledEventEventStoreJpaAdapter;
import com.webhook.relay.chatterbox.adapter.out.persistence.poll.GithubPolledEvent;
import com.webhook.relay.chatterbox.adapter.out.persistence.poll.repository.GithubPolledEventJpaRepository;
import com.webhook.relay.chatterbox.adapter.out.persistence.webhook.WebhookEvent;
import com.webhook.relay.chatterbox.adapter.out.persistence.webhook.repository.WebhookEventJpaRepository;
import com.webhook.relay.chatterbox.adapter.out.webhook.mapper.GithubWebhookEventMapper;
import com.webhook.relay.chatterbox.adapter.out.webhook.poll.GithubRestPollingClient;
import com.webhook.relay.chatterbox.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import com.webhook.relay.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import com.webhook.relay.chatterbox.application.domain.delivery.RepositoryDetail;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventStatus;
import com.webhook.relay.chatterbox.application.domain.event.model.WebhookEventType;
import com.webhook.relay.chatterbox.application.domain.persistence.WebhookPolledEventReceived;
import com.webhook.relay.chatterbox.common.config.InfrastructurePropertiesConfig;
import com.webhook.relay.chatterbox.common.logging.convenience.ImportSlf4jWebhookLogger;
import com.webhook.relay.chatterbox.common.logging.mdc.Slf4jMdcContext;
import com.webhook.relay.chatterbox.test.container.AbstractPostgresTestContainer;
import com.webhook.relay.chatterbox.test.helper.JsonFileReader;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ImportSlf4jWebhookLogger
@Import({
        WebhookOrchestrator.class,
        JsonFileReader.class,
        GithubWebhookValidator.class,
        GithubWebhookEventMapper.class,
        JacksonJsonConverter.class,
        InfrastructurePropertiesConfig.class,
        GithubRestPollingClient.class,
        PropertiesConfigurationResolver.class,
        WebhookPolledEventEventStoreJpaAdapter.class,
        Slf4jMdcContext.class,
        WebhookEventStoreJpaAdapter.class,
        WebhookPolledEventEventStoreJpaAdapter.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles({"test", "live-url"})
@MirrorProductionClassForArchitectureRuleTests(WebhookOrchestrator.class)
public class WebhookOrchestratorPollGithubIT extends AbstractPostgresTestContainer {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @Autowired
    private WebhookEventJpaRepository webhookEventJpaRepository;

    @Autowired
    private GithubPolledEventJpaRepository githubPolledEventJpaRepository;

    @Autowired
    private WebhookOrchestrator webhookOrchestrator;

    private static Stream<Arguments> repositoryDetails() {
        return Stream.of(
                Arguments.of(Named.of("Chatterbox", new RepositoryDetail("alexBlakeGoudemond", "chatterbox", "2025-12-15T06:00:00", "2025-12-16T06:00:00"))),
                Arguments.of(Named.of("SoftwareFoundations", new RepositoryDetail("Company-Knowledge-Repository", "mentoring_software_foundations", "2025-11-26T06:00:00", "2025-11-27T06:00:00")))
        );
    }

    @ParameterizedTest(name = "RecentChanges; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void whenPollRecentChanges_ThenSuccess(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryFullName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();

        List<WebhookPolledEventReceived> githubPolledEvents = webhookOrchestrator.pollForChanges(owner, repositoryFullName, fromDate, untilDate);
        assertNotNull(githubPolledEvents);
        assertFalse(githubPolledEvents.isEmpty());
        for (WebhookPolledEventReceived polledEvent : githubPolledEvents) {
            assertNotNull(polledEvent.id());
        }
    }

    @DisplayName("With previous Webhook Received; Polling does not duplicate")
    @Test
    public void givenPreviouslyProcessedWebhookAndNoMatchingPolledEvent_WhenPollForChanges_ThenNoDuplicateChangesReturned() {
        String repositoryFullName = "alexBlakeGoudemond/chatterbox";
        String[] repositoryDetails = repositoryFullName.split("/");
        String owner = repositoryDetails[0];
        String repositoryName = repositoryDetails[1];
        LocalDateTime fromDate = LocalDateTime.parse("2026-02-14T22:27:39");
        LocalDateTime toDate = LocalDateTime.parse("2026-02-15T10:00:00");
        saveWebhookEvent(repositoryFullName, fromDate, WebhookEventStatus.PROCESSED_SUCCESS);

        List<WebhookPolledEventReceived> webhookPolledEvents = webhookOrchestrator.pollForChanges(owner, repositoryName, fromDate, toDate);
        assertNotNull(webhookPolledEvents);
        assertTrue(webhookPolledEvents.isEmpty());
    }

    @DisplayName("With previous Webhook Received and Poll Successful; Polling does not duplicate")
    @Test
    public void givenPreviouslyProcessedWebhookAndMatchingPolledEvent_WhenPollForChanges_ThenNoDuplicateChangesReturned() {
        String repositoryFullName = "alexBlakeGoudemond/chatterbox";
        String[] repositoryDetails = repositoryFullName.split("/");
        String owner = repositoryDetails[0];
        String repositoryName = repositoryDetails[1];
        LocalDateTime fromDate = LocalDateTime.parse("2026-02-14T22:27:39");
        LocalDateTime toDate = LocalDateTime.parse("2026-02-15T10:00:00");
        saveWebhookEvent(repositoryFullName, fromDate, WebhookEventStatus.PROCESSED_SUCCESS);
        saveWebhookPolledEvent(repositoryFullName, fromDate, WebhookEventStatus.PROCESSED_SUCCESS);

        List<WebhookPolledEventReceived> webhookPolledEvents = webhookOrchestrator.pollForChanges(owner, repositoryName, fromDate, toDate);
        assertNotNull(webhookPolledEvents);
        assertTrue(webhookPolledEvents.isEmpty());
    }

    @SuppressWarnings("SameParameterValue")
    private void saveWebhookEvent(String repositoryFullName, LocalDateTime fromDate, WebhookEventStatus webhookEventStatus) {
        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setRepositoryFullName(repositoryFullName);
        webhookEvent.setWebhookId("abc123");
        webhookEvent.setWebhookEventType(WebhookEventType.PULL_REQUEST);
        webhookEvent.setDisplayName("Dummy Display Name");
        webhookEvent.setSenderName("Dummy Sender Name");
        webhookEvent.setEventUrl("Dummy Event URL");
        webhookEvent.setEventUrlDisplayText("Dummy Event URL Display Text");
        webhookEvent.setExtraDetail("Dummy Extra Detail");
        webhookEvent.setPayload("{}");
        webhookEvent.setWebhookEventStatus(webhookEventStatus);
        webhookEvent.setReceivedAt(fromDate);

        WebhookEvent persistedWebhookEvent = webhookEventJpaRepository.save(webhookEvent);
        assertNotNull(persistedWebhookEvent);
        assertTrue(persistedWebhookEvent.getId() > 0L);
    }

    @SuppressWarnings("SameParameterValue")
    private void saveWebhookPolledEvent(String repositoryFullName, LocalDateTime fromDate, WebhookEventStatus webhookEventStatus) {
        GithubPolledEvent githubPolledEvent = new GithubPolledEvent();
        githubPolledEvent.setRepositoryFullName(repositoryFullName);
        githubPolledEvent.setSourceId("abc123");
        githubPolledEvent.setWebhookEventType(WebhookEventType.PULL_REQUEST);
        githubPolledEvent.setDisplayName("Dummy Display Name");
        githubPolledEvent.setSenderName("Dummy Sender Name");
        githubPolledEvent.setEventUrl("Dummy Event URL");
        githubPolledEvent.setEventUrlDisplayText("Dummy Event URL Display Text");
        githubPolledEvent.setExtraDetail("Dummy Extra Detail");
        githubPolledEvent.setPayload("{}");
        githubPolledEvent.setWebhookEventStatus(webhookEventStatus);
        githubPolledEvent.setFetchedAt(fromDate);

        GithubPolledEvent persistedPolledEvent = githubPolledEventJpaRepository.save(githubPolledEvent);
        assertNotNull(persistedPolledEvent);
        assertTrue(persistedPolledEvent.getId() > 0L);
    }

}