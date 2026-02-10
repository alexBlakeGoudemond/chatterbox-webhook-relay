package za.co.psybergate.chatterbox.application.domain.event.notification;

import lombok.Getter;
import lombok.ToString;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookEventReceived;

import java.time.LocalDateTime;

@Getter
@ToString
public class WebhookEventProcessed {

    private final String webhookTrackingUuid;

    private final String repositoryFullName;

    private final LocalDateTime eventDateTime;

    public WebhookEventProcessed(String webhookTrackingUuid, String repositoryFullName) {
        this.webhookTrackingUuid = webhookTrackingUuid;
        this.repositoryFullName = repositoryFullName;
        this.eventDateTime = LocalDateTime.now();
    }

    public WebhookEventProcessed(String webhookTrackingUuid, WebhookEventReceived webhookEventReceived) {
        this(webhookTrackingUuid, webhookEventReceived.repositoryFullName());
    }

}
