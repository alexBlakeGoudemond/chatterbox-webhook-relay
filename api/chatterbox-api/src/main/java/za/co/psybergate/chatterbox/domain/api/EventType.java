package za.co.psybergate.chatterbox.domain.api;

import lombok.Getter;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;

/// The EventTypes defined here are available via the
/// Github API:
/// [Github Event Types](https://docs.github.com/en/rest/using-the-rest-api/github-event-types?apiVersion=2022-11-28)
///
/// These properties may also be expected by Services and thus must
/// be defined in the properties file
@Getter
public enum EventType {

    PUSH,
    PULL_REQUEST,
    POLL_COMMIT,
    POLL_PULL_REQUEST;

    public static boolean contains(String eventMapping) {
        for (EventType eventType : values()) {
            if (eventType.name().equals(eventMapping))
                return true;
        }
        return false;
    }

    public static EventType get(String eventMapping) {
        for (EventType eventType : values()) {
            if (eventType.name().equalsIgnoreCase(eventMapping))
                return eventType;
        }
        throw new ApplicationException("Unknown event type " + eventMapping);
    }

}
