package za.co.psybergate.chatterbox.domain.api;

import lombok.Getter;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;

/// The properties defined here are common JSON keys
/// that the Github API includes in the JSON Response Payloads.
///
/// They can be used for reference and inspiration when reviewing
/// the properties defined through [ChatterboxSourceGithubPayloadProperties]
@Getter
public enum GithubApiJsonKeys {

    FULL_NAME("full_name");

    GithubApiJsonKeys(String value) {
        this.value = value;
    }

    private final String value;

}
