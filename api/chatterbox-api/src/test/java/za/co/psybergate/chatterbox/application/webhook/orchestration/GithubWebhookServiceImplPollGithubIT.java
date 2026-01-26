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
import za.co.psybergate.chatterbox.application.port.in.webhook.orchestration.GithubWebhookPort;
import za.co.psybergate.chatterbox.application.common.logging.Slf4jWebhookLogger;
import za.co.psybergate.chatterbox.application.common.web.serialisation.JacksonJsonConverter;
import za.co.psybergate.chatterbox.application.common.webhook.mapper.GithubWebhookEventMapper;
import za.co.psybergate.chatterbox.application.usecase.webhook.orchestration.GithubWebhookOrchestrator;
import za.co.psybergate.chatterbox.application.domain.delivery.model.RepositoryDetailDto;
import za.co.psybergate.chatterbox.application.domain.event.model.GithubPolledEventDto;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.validation.GithubWebhookValidator;
import za.co.psybergate.chatterbox.infrastructure.common.config.InfrastructurePropertiesConfig;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.actuator.WebhookRuntimeMetrics;
import za.co.psybergate.chatterbox.infrastructure.adapter.in.web.filter.WebhookFilter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.github.delivery.GithubRestPollingClient;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.GithubPolledEventEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.persistence.WebhookEventStoreJpaAdapter;
import za.co.psybergate.chatterbox.infrastructure.adapter.out.webhook.resolution.PropertiesConfigurationResolver;
import za.co.psybergate.chatterbox.test.container.AbstractPostgresTestContainer;
import za.co.psybergate.chatterbox.test.helper.JsonFileReader;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        GithubWebhookOrchestrator.class,
        JsonFileReader.class,
        GithubWebhookValidator.class,
        GithubWebhookEventMapper.class,
        JacksonJsonConverter.class,
        InfrastructurePropertiesConfig.class,
        Slf4jWebhookLogger.class,
        GithubRestPollingClient.class,
        PropertiesConfigurationResolver.class,
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
    private GithubWebhookPort githubWebhookPort;

    private static Stream<Arguments> repositoryDetails() {
        return Stream.of(
                Arguments.of(Named.of("Chatterbox", new RepositoryDetailDto("psyAlexBlakeGoudemond", "chatterbox", "2025-12-15T06:00:00", "2025-12-16T06:00:00"))),
                Arguments.of(Named.of("SoftwareFoundations", new RepositoryDetailDto("Psybergate-Knowledge-Repository", "mentoring_software_foundations", "2025-11-26T06:00:00", "2025-11-27T06:00:00")))
        );
    }

    @ParameterizedTest(name = "RecentChanges; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void whenPollRecentChanges_ThenSuccess(RepositoryDetailDto repositoryDetailDto) {
        String owner = repositoryDetailDto.repositoryOwner();
        String repositoryFullName = repositoryDetailDto.repositoryName();
        LocalDateTime fromDate = repositoryDetailDto.fromDate();
        LocalDateTime untilDate = repositoryDetailDto.toDate();

        List<GithubPolledEventDto> githubPolledEvents = githubWebhookPort.pollGithubForChanges(owner, repositoryFullName, fromDate, untilDate);
        assertNotNull(githubPolledEvents);
        assertFalse(githubPolledEvents.isEmpty());
        for (GithubPolledEventDto polledEvent : githubPolledEvents) {
            assertNotNull(polledEvent.id());
        }
    }

}