package za.co.psybergate.chatterbox.domain.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@ToString
public class GithubPolledEventRecord {

    private Long id;

    private String repositoryFullName;

    private String sourceId;

    private EventType eventType;

    private String displayName;

    private String senderName;

    private String eventUrl;

    private String eventUrlDisplayText;

    private String extraDetail;

    private String payload;

    private EventStatus eventStatus;

    private String errorMessage;

    private LocalDateTime fetchedAt;

    private LocalDateTime processedAt;

    public GithubPolledEventRecord(
            Long id,
            String repositoryFullName,
            String sourceId,
            EventType eventType,
            String displayName,
            String senderName,
            String eventUrl,
            String eventUrlDisplayText,
            String extraDetail,
            String payload,
            EventStatus eventStatus,
            String errorMessage,
            LocalDateTime fetchedAt,
            LocalDateTime processedAt
            ) {
        this.id = id;
        this.repositoryFullName = repositoryFullName;
        this.sourceId = sourceId;
        this.eventType = eventType;
        this.displayName = displayName;
        this.senderName = senderName;
        this.eventUrl = eventUrl;
        this.eventUrlDisplayText = eventUrlDisplayText;
        this.extraDetail = extraDetail;
        this.payload = payload;
        this.eventStatus = eventStatus;
        this.errorMessage = errorMessage;
        this.fetchedAt = fetchedAt;
        this.processedAt = processedAt;
    }

}
