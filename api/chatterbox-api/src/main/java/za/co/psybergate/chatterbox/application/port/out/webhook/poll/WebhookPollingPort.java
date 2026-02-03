package za.co.psybergate.chatterbox.application.port.out.webhook.poll;

import za.co.psybergate.chatterbox.application.domain.event.model.RepositoryUpdates;

import java.time.LocalDateTime;
import java.util.List;

// TODO BlakeGoudemond 2026/01/17 | test this class?
public interface WebhookPollingPort {

    RepositoryUpdates getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate);

    RepositoryUpdates getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<?> getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate);

    List<?> getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    List<?> getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate);

    List<?> getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

}
