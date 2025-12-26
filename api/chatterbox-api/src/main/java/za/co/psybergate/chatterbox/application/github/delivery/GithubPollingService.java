package za.co.psybergate.chatterbox.application.github.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;

import java.time.LocalDateTime;

public interface GithubPollingService {

    GithubRepositoryInformationDto getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate);

    GithubRepositoryInformationDto getRecentUpdates(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    JsonNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate);

    /// [API Contract for Commits](https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28)  JsonNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate); JsonNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate);
    JsonNode getCommitsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

    JsonNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate);

    /// [API Contract for Pull Requests](https://docs.github.com/en/rest/pulls/pulls?apiVersion=2022-11-28#list-pull-requests)  JsonNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);
    JsonNode getPullRequestsSince(String owner, String repositoryName, LocalDateTime fromDate, LocalDateTime untilDate);

}
