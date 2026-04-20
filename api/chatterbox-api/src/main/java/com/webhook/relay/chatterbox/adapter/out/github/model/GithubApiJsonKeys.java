package com.webhook.relay.chatterbox.adapter.out.github.model;

import lombok.Getter;

@Getter
public enum GithubApiJsonKeys {

    FULL_NAME("full_name"), MERGED_AT("merged_at");

    private final String value;

    GithubApiJsonKeys(String value) {
        this.value = value;
    }

}
