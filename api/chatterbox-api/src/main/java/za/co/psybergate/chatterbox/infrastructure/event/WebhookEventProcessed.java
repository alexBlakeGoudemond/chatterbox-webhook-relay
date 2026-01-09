package za.co.psybergate.chatterbox.infrastructure.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WebhookEventProcessed {

    private final LocalDateTime eventDateTime;

    public WebhookEventProcessed() {
        eventDateTime = LocalDateTime.now();
    }

}
