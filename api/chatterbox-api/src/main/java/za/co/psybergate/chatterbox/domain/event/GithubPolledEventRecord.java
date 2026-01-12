package za.co.psybergate.chatterbox.domain.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
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

}
