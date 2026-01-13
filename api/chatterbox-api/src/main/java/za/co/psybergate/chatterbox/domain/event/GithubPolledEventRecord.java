package za.co.psybergate.chatterbox.domain.event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.domain.dto.GithubEventDto;
import za.co.psybergate.chatterbox.infrastructure.persistence.poll.GithubPolledEvent;

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

    public GithubPolledEventRecord(GithubPolledEvent polledEvent) {
        this.id = polledEvent.getId();
        this.repositoryFullName = polledEvent.getRepositoryFullName();
        this.sourceId = polledEvent.getSourceId();
        this.eventType = polledEvent.getEventType();
        this.displayName = polledEvent.getDisplayName();
        this.senderName = polledEvent.getSenderName();
        this.eventUrl = polledEvent.getEventUrl();
        this.eventUrlDisplayText = polledEvent.getEventUrlDisplayText();
        this.extraDetail = polledEvent.getExtraDetail();
        this.payload = polledEvent.getPayload();
        this.eventStatus = polledEvent.getEventStatus();
        this.errorMessage = polledEvent.getErrorMessage();
        this.fetchedAt = polledEvent.getFetchedAt();
        this.processedAt = polledEvent.getProcessedAt();
    }

}
