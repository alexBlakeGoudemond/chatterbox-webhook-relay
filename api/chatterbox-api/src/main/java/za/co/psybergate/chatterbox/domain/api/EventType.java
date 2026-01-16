package za.co.psybergate.chatterbox.domain.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import za.co.psybergate.chatterbox.domain.exception.DomainException;

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
        throw new DomainException("Unknown event type " + eventMapping);
    }

    public String getUniqueId(JsonNode jsonNode) {
        return switch (this) {
            case POLL_COMMIT -> jsonNode.get("sha").asText();
            case POLL_PULL_REQUEST -> jsonNode.get("merge_commit_sha").asText();
            default -> throw new DomainException("Unable to find UniqueID; Unknown event type " + this);
        };
    }
}
