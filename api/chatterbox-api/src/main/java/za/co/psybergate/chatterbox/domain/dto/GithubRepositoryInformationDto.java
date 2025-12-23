package za.co.psybergate.chatterbox.domain.dto;

import jakarta.validation.constraints.NotNull;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPullRequest;

import java.time.LocalDateTime;
import java.util.List;

public record GithubRepositoryInformationDto(
        @NotNull LocalDateTime fromDate,
        @NotNull LocalDateTime untilDate,
        @NotNull List<GHPullRequest> pullRequests,
        @NotNull List<GHCommit> commits
) {

}
