package za.co.psybergate.chatterbox.application.common.logging.detail;

import java.time.LocalDateTime;

public interface PollingLogger {

    void logPollRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    void logPollEventType(String eventType, String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

}
