package com.webhook.relay.chatterbox.application.domain.event.notification;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class PolledEventsProcessed {

    private final LocalDateTime eventDateTime;

    public PolledEventsProcessed() {
        this.eventDateTime = LocalDateTime.now();
    }

}
