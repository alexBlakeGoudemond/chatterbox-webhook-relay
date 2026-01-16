package za.co.psybergate.chatterbox.application.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class WebhookEventProcessed {

    private final LocalDateTime eventDateTime;

    public WebhookEventProcessed() {
        eventDateTime = LocalDateTime.now();
    }

}
