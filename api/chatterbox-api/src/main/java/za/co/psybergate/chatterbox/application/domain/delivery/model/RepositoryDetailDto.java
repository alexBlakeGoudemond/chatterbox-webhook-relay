package za.co.psybergate.chatterbox.application.domain.delivery.model;

import java.time.LocalDateTime;

public record RepositoryDetailDto(String repositoryOwner, String repositoryName, LocalDateTime fromDate,
                                  LocalDateTime toDate) {

    public RepositoryDetailDto(String repositoryOwner, String repositoryName, String fromDate, String toDate) {
        this(repositoryOwner, repositoryName, LocalDateTime.parse(fromDate), LocalDateTime.parse(toDate));
    }

}
