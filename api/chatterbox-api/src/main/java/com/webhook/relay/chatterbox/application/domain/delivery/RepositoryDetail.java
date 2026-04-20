package com.webhook.relay.chatterbox.application.domain.delivery;

import java.time.LocalDateTime;

public record RepositoryDetail(
        String repositoryOwner,
        String repositoryName,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {

    public RepositoryDetail(String repositoryOwner, String repositoryName, String fromDate, String toDate) {
        this(repositoryOwner, repositoryName, LocalDateTime.parse(fromDate), LocalDateTime.parse(toDate));
    }

}
