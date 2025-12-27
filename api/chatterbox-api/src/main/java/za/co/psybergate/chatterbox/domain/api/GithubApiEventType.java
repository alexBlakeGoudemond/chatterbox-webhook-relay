package za.co.psybergate.chatterbox.domain.api;

import lombok.Getter;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;

/// The EventTypes defined here are available via the
/// Github API:
/// [Github Event Types](https://docs.github.com/en/rest/using-the-rest-api/github-event-types?apiVersion=2022-11-28)
///
/// These Event Types can be defined in the properties files and loaded
/// through [ChatterboxSourceGithubPayloadProperties].
///
/// These properties may also be expected by Services and thus must
/// be defined in the properties file
@Getter
public enum GithubApiEventType {

    PUSH("push"),
    PULL_REQUEST("pull_request"),
    POLL_COMMIT("poll_commit"),
    POLL_PULL_REQUEST("poll_pull_request");

    GithubApiEventType(String value) {
        this.value = value;
    }

    private final String value;

    public static boolean contains(String eventMapping) {
        for (GithubApiEventType eventType : values()) {
            if (eventType.value.equals(eventMapping))
                return true;
        }
        return false;
    }

    public static GithubApiEventType get(String eventMapping) {
        for (GithubApiEventType eventType : values()) {
            if (eventType.value.equals(eventMapping))
                return eventType;
        }
        throw new ApplicationException("Unknown event type " + eventMapping);
    }

}
