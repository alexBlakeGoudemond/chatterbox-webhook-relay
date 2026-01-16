package za.co.psybergate.chatterbox.application.webhook.orchestration;

import org.junit.jupiter.api.Named;
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
import za.co.psybergate.chatterbox.infrastructure.logging.WebhookLoggerImpl;
import za.co.psybergate.chatterbox.domain.dto.RepositoryDetail;
import za.co.psybergate.chatterbox.application.persistence.dto.GithubPolledEventDto;
import za.co.psybergate.chatterbox.infrastructure.web.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.config.ApplicationConfig;
import za.co.psybergate.chatterbox.infrastructure.github.delivery.GithubPollingServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEventEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.web.serialisation.JsonConverterImpl;
import za.co.psybergate.chatterbox.infrastructure.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.webhook.orchestration.GithubWebhookServiceImpl;
import za.co.psybergate.chatterbox.infrastructure.webhook.validation.WebhookRequestValidatorImpl;
import za.co.psybergate.chatterbox.infrastructure.webhook.mapper.GithubEventMapperImpl;
import za.co.psybergate.chatterbox.infrastructure.webhook.resolution.WebhookConfigurationResolverImpl;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        GithubWebhookServiceImpl.class,
        JsonFileReader.class,
        WebhookRequestValidatorImpl.class,
        GithubEventMapperImpl.class,
        JsonConverterImpl.class,
        ApplicationConfig.class,
        WebhookLoggerImpl.class,
        GithubPollingServiceImpl.class,
        WebhookConfigurationResolverImpl.class,
        GithubPolledEventEventStoreJpaAdapter.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles({"test", "live-url"})
public class GithubWebhookServiceImplPollGithubIT extends AbstractPostgresTestContainer {

    @MockitoBean
    private WebhookFilter webhookFilter;

    @MockitoBean
    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @MockitoBean
    private WebhookEventStoreJpaAdapter webhookReceivedStore;

    @Autowired
    private GithubWebhookService githubWebhookService;

    private static Stream<Arguments> repositoryDetails() {
        return Stream.of(
                Arguments.of(Named.of("Chatterbox", new RepositoryDetail("psyAlexBlakeGoudemond", "chatterbox", "2025-12-15T06:00:00", "2025-12-16T06:00:00"))),
                Arguments.of(Named.of("SoftwareFoundations", new RepositoryDetail("Psybergate-Knowledge-Repository", "mentoring_software_foundations", "2025-11-26T06:00:00", "2025-11-27T06:00:00")))
        );
    }

    @ParameterizedTest(name = "RecentChanges; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void whenPollRecentChanges_ThenSuccess(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryFullName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();

        List<GithubPolledEventDto> githubPolledEvents = githubWebhookService.pollGithubForChanges(owner, repositoryFullName, fromDate, untilDate);
        assertNotNull(githubPolledEvents);
        assertFalse(githubPolledEvents.isEmpty());
        for (GithubPolledEventDto polledEvent : githubPolledEvents) {
            assertNotNull(polledEvent.id());
        }
    }

}