package za.co.psybergate.chatterbox.application.domain.api;

import lombok.Getter;

// TODO BlakeGoudemond 2026/01/27 | what if we move to gitlab? should move
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
