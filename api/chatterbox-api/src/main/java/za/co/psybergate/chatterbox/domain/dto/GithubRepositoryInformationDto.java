package za.co.psybergate.chatterbox.domain.dto;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPullRequest;

import java.time.LocalDateTime;
import java.util.List;

public record GithubRepositoryInformationDto(
        LocalDateTime updatesSince,
        List<GHPullRequest> pullRequests,
        List<GHCommit> commits
) {

}
