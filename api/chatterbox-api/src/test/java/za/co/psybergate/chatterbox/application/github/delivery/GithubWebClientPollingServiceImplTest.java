package za.co.psybergate.chatterbox.application.github.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles({"test", "live-url"})
class GithubWebClientPollingServiceImplTest {

    @Autowired
    private GithubWebClientPollingServiceImpl pollingService;

    // TODO BlakeGoudemond 2025/12/23 | build up from here; also add event mapping for commits for this
    @Test
    public void getCommitsSince() {
        JsonNode commitsSince = pollingService.getCommitsSince("psyAlexBlakeGoudemond", "chatterbox", LocalDateTime.parse("2025-12-15T06:00:00"));
        System.out.println("commitsSince = " + commitsSince);
    }

}