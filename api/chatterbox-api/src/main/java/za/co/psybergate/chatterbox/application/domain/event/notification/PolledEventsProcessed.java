package za.co.psybergate.chatterbox.application.domain.event.notification;

import lombok.Getter;
import lombok.ToString;
import za.co.psybergate.chatterbox.application.domain.persistence.WebhookPolledEventReceived;

import java.time.LocalDateTime;

@Getter
@ToString
public class PolledEventsProcessed {

    private final String webhookTrackingUuid;

    private final LocalDateTime eventDateTime;

    public PolledEventsProcessed(String webhookTrackingUuid) {
        this.webhookTrackingUuid = webhookTrackingUuid;
        this.eventDateTime = LocalDateTime.now();
    }

}
