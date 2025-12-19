package za.co.psybergate.chatterbox.application.github.delivery;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GithubPollingService {

    List<GHCommit> getCommitsSince(GHRepository repository, LocalDateTime lastReceivedUpdate);

    List<GHPullRequest> getPullRequestsSince(GHRepository repository, LocalDateTime lastReceivedUpdate);

}
