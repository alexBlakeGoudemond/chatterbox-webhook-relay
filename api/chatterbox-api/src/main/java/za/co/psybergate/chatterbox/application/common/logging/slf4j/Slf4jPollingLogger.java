package za.co.psybergate.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.detail.PollingLogger;

import java.time.LocalDateTime;

@Slf4j
@Component
public class Slf4jPollingLogger implements PollingLogger {

    @Override
    public void logPollRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        log.info("[Polling] querying '{}/{}' for any updates since {} - {}", owner, repositoryName, fromDate, untilDate);
    }

    @Override
    public void logPollEventType(String eventType, String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate) {
        log.debug("[Polling] querying if any {} occurred for '{}/{}' since {} - {}", eventType, owner, repositoryName, fromDate, untilDate);
    }

}
