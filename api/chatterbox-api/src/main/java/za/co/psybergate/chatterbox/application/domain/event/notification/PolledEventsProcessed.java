package za.co.psybergate.chatterbox.application.domain.event.notification;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

// TODO BlakeGoudemond 2026/01/30 | should we have a github impl of this - could poll Bitbucket?
@Getter
@ToString
public class PolledEventsProcessed {

    private final LocalDateTime eventDateTime;

    public PolledEventsProcessed() {
        this.eventDateTime = LocalDateTime.now();
    }

}
