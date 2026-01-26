package za.co.psybergate.chatterbox.application.domain.api;

import lombok.Getter;

/// The properties defined here are common JSON keys
/// that the Github API includes in the JSON Response Payloads.
@Getter
public enum GithubApiJsonKeys {

    FULL_NAME("full_name");

    private final String value;

    GithubApiJsonKeys(String value) {
        this.value = value;
    }

}
