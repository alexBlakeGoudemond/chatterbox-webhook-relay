package za.co.psybergate.chatterbox.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GithubRepositoryInformationDto(
        @NotNull LocalDateTime fromDate,
        @NotNull LocalDateTime untilDate,
        @NotNull JsonNode pullRequests,
        @NotNull JsonNode commits
) {

}
