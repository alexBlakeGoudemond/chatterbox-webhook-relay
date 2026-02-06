package za.co.psybergate.chatterbox.application.port.out.webhook.poll;

import za.co.psybergate.chatterbox.application.domain.event.model.RawEventPayload;
import za.co.psybergate.chatterbox.application.domain.event.model.RepositoryUpdates;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookPollingPort {

    RepositoryUpdates getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate);

    RepositoryUpdates getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<RawEventPayload> getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate);

    List<RawEventPayload> getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<RawEventPayload> getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate);

    List<RawEventPayload> getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

}
