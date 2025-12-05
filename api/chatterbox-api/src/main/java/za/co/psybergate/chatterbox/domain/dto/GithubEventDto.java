package za.co.psybergate.chatterbox.domain.dto;

public record GithubEventDto(
        String eventType,
        String displayName,
        String repositoryName,
        String senderName,
        String url,
        String urlDisplayText
) {

}
