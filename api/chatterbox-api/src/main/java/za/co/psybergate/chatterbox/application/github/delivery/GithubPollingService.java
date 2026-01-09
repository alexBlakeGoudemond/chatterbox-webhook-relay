package za.co.psybergate.chatterbox.application.github.delivery;

import com.fasterxml.jackson.databind.node.ArrayNode;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface GithubPollingService {

    GithubRepositoryInformationDto getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate);

    GithubRepositoryInformationDto getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    ArrayNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate);

    /// [API Contract for Commits](https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28)
    ArrayNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    ArrayNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate);

    /// [API Contract for Pull Requests](https://docs.github.com/en/rest/pulls/pulls?apiVersion=2022-11-28#list-pull-requests)
    ArrayNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

}
