package za.co.psybergate.chatterbox.application.port.out.webhook.poll;

import com.fasterxml.jackson.databind.node.ArrayNode;
import za.co.psybergate.chatterbox.application.domain.event.model.RepositoryUpdates;

import java.time.LocalDateTime;

// TODO BlakeGoudemond 2026/01/17 | test this class?
public interface WebhookPollingPort {

    RepositoryUpdates getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate);

    RepositoryUpdates getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    ArrayNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate);

    ArrayNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    ArrayNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate);

    ArrayNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

}
