package za.co.psybergate.chatterbox.domain.record;

import lombok.Data;
import lombok.ToString;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;

import java.time.LocalDateTime;

// TODO BlakeGoudemond 2026/01/16 | convert to record
@Data
@ToString
public class WebhookEventRecord {

    private Long id;

    private String repositoryFullName;

    private String webhookId;

    private EventType eventType;

    private String displayName;

    private String senderName;

    private String eventUrl;

    private String eventUrlDisplayText;

    private String extraDetail;

    private String payload;

    private EventStatus eventStatus;

    private String errorMessage;

    private LocalDateTime receivedAt;

    private LocalDateTime processedAt;

    public WebhookEventRecord(Long id,
                              String repositoryFullName,
                              String webhookId,
                              EventType eventType,
                              String displayName,
                              String senderName,
                              String eventUrl,
                              String eventUrlDisplayText,
                              String extraDetail,
                              String payload,
                              EventStatus eventStatus,
                              String errorMessage,
                              LocalDateTime receivedAt,
                              LocalDateTime processedAt) {
        this.id = id;
        this.repositoryFullName = repositoryFullName;
        this.webhookId = webhookId;
        this.eventType = eventType;
        this.displayName = displayName;
        this.senderName = senderName;
        this.eventUrl = eventUrl;
        this.eventUrlDisplayText = eventUrlDisplayText;
        this.extraDetail = extraDetail;
        this.payload = payload;
        this.eventStatus = eventStatus;
        this.errorMessage = errorMessage;
        this.receivedAt = receivedAt;
        this.processedAt = processedAt;
    }

}
