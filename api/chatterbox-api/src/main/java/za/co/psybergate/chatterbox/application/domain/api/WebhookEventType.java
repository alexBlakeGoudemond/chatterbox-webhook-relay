package za.co.psybergate.chatterbox.application.domain.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import za.co.psybergate.chatterbox.application.domain.exception.DomainException;

/// These properties may also be expected by Services and thus must
/// be defined in the properties file
@Getter
public enum WebhookEventType {

    PUSH,
    PULL_REQUEST,
    POLL_COMMIT,
    POLL_PULL_REQUEST;

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

    public String getUniqueId(JsonNode jsonNode) {
        return switch (this) {
            case POLL_COMMIT -> jsonNode.get("sha").asText();
            case POLL_PULL_REQUEST -> jsonNode.get("merge_commit_sha").asText();
            default -> throw new DomainException("Unable to find UniqueID; Unknown event type " + this);
        };
    }
}
