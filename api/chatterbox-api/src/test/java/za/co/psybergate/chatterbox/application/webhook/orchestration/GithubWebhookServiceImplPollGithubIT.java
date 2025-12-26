package za.co.psybergate.chatterbox.application.webhook.orchestration;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import za.co.psybergate.chatterbox.domain.dto.RepositoryDetail;

import java.time.LocalDateTime;
import java.util.stream.Stream;

// TODO BlakeGoudemond 2025/12/21 | reduce scope later
@SpringBootTest
@ActiveProfiles({"test", "live-url"})
public class GithubWebhookServiceImplPollGithubIT {

    @Autowired
    private GithubWebhookService githubWebhookService;

    // TODO BlakeGoudemond 2025/12/26 | add PR name to commits etc?
    @Tag("live-integration")
    @ParameterizedTest(name = "RecentChanges; {index}: repo:{0}")
    @MethodSource("repositoryDetails")
    public void testGithubWebhookService(RepositoryDetail repositoryDetail) {
        String owner = repositoryDetail.repositoryOwner();
        String repositoryFullName = repositoryDetail.repositoryName();
        LocalDateTime fromDate = repositoryDetail.fromDate();
        LocalDateTime untilDate = repositoryDetail.toDate();

        githubWebhookService.pollGithubForChanges(owner, repositoryFullName, fromDate, untilDate);
    }

    private static Stream<Arguments> repositoryDetails() {
        return Stream.of(
                Arguments.of(Named.of("Chatterbox", new RepositoryDetail("psyAlexBlakeGoudemond", "chatterbox", "2025-12-15T06:00:00", "2025-12-16T06:00:00"))),
                Arguments.of(Named.of("SoftwareFoundations", new RepositoryDetail("Psybergate-Knowledge-Repository", "mentoring_software_foundations", "2025-11-26T06:00:00", "2025-11-27T06:00:00")))
        );
    }

}