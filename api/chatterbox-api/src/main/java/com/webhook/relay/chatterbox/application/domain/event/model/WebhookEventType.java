package com.webhook.relay.chatterbox.application.domain.event.model;

import lombok.Getter;
import com.webhook.relay.chatterbox.application.domain.exception.DomainException;

/// These properties may also be expected by Services and thus must
/// be defined in the properties file
@Getter
public enum WebhookEventType {

    PUSH,
    PULL_REQUEST,
    POLL_COMMIT,
    POLL_PULL_REQUEST,
    PING;

    public static boolean contains(String eventMapping) {
        for (WebhookEventType webhookEventType : values()) {
            if (webhookEventType.name().equals(eventMapping))
                return true;
        }
        return false;
    }

    public static WebhookEventType get(String eventMapping) {
        for (WebhookEventType webhookEventType : values()) {
            if (webhookEventType.name().equalsIgnoreCase(eventMapping))
                return webhookEventType;
        }
        throw new DomainException("Unknown event type " + eventMapping);
    }

}
