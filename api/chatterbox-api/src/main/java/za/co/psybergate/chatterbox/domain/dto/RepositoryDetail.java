package za.co.psybergate.chatterbox.domain.dto;

import java.time.LocalDateTime;

public record RepositoryDetail(String repositoryFullName, LocalDateTime fromDate, LocalDateTime toDate) {

    public RepositoryDetail(String repositoryFullName, String fromDate, String toDate) {
        this(repositoryFullName, LocalDateTime.parse(fromDate), LocalDateTime.parse(toDate));
    }

}
