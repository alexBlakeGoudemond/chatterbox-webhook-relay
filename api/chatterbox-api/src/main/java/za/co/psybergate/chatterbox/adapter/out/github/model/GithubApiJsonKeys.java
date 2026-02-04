package za.co.psybergate.chatterbox.adapter.out.github.model;

import lombok.Getter;

@Getter
public enum GithubApiJsonKeys {

    FULL_NAME("full_name");

    private final String value;

    GithubApiJsonKeys(String value) {
        this.value = value;
    }

}
