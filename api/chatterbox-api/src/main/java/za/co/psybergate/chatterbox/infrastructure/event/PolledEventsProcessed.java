package za.co.psybergate.chatterbox.infrastructure.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PolledEventsProcessed {

    private final LocalDateTime eventDateTime;

    public PolledEventsProcessed() {
        this.eventDateTime = LocalDateTime.now();
    }

}
