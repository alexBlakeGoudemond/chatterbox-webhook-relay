package za.co.psybergate.chatterbox.domain.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import za.co.psybergate.chatterbox.domain.api.EventStatus;
import za.co.psybergate.chatterbox.domain.api.EventType;
import za.co.psybergate.chatterbox.infrastructure.persistence.webhook.WebhookEvent;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
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

    public WebhookEventRecord(WebhookEvent webhookEvent) {
        this.id = webhookEvent.getId();
        this.repositoryFullName = webhookEvent.getRepositoryFullName();
        this.webhookId = webhookEvent.getWebhookId();
        this.eventType = webhookEvent.getEventType();
        this.displayName = webhookEvent.getDisplayName();
        this.senderName = webhookEvent.getSenderName();
        this.eventUrl = webhookEvent.getEventUrl();
        this.eventUrlDisplayText = webhookEvent.getEventUrlDisplayText();
        this.extraDetail = webhookEvent.getExtraDetail();
        this.payload = webhookEvent.getPayload();
        this.eventStatus = webhookEvent.getEventStatus();
        this.errorMessage = webhookEvent.getErrorMessage();
        this.receivedAt = webhookEvent.getReceivedAt();
        this.processedAt = webhookEvent.getProcessedAt();
    }

}
