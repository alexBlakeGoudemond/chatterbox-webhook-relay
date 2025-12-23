package za.co.psybergate.chatterbox.application.github.delivery;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import za.co.psybergate.chatterbox.domain.dto.GithubRepositoryInformationDto;

import java.time.LocalDateTime;
import java.util.List;

public interface GithubPollingService {

    GithubRepositoryInformationDto getRecentUpdates(String repositoryFullName, LocalDateTime lastReceivedDate);

    GithubRepositoryInformationDto getRecentUpdates(GHRepository repository, LocalDateTime fromDate, LocalDateTime untilDate);

    GHRepository getGithubRepository(String repositoryFullName);

    List<GHCommit> getCommitsSince(GHRepository repository, LocalDateTime lastReceivedUpdate);

    List<GHCommit> getCommitsSince(GHRepository repository, LocalDateTime startDate, LocalDateTime endDate);

    List<GHPullRequest> getPullRequestsSince(GHRepository repository, LocalDateTime lastReceivedUpdate);

    List<GHPullRequest> getPullRequestsSince(GHRepository repository, LocalDateTime startDate, LocalDateTime endDate);

}
