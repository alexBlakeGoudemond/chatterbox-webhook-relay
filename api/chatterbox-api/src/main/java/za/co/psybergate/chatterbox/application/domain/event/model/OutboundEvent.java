package com.webhook.relay.chatterbox.application.domain.event.model;

import jakarta.validation.constraints.NotNull;

public record OutboundEvent(
        Long id,
        String sourceId,
        @NotNull String type,
        @NotNull String title,
        @NotNull String repository,
        @NotNull String actor,
        @NotNull String url,
        @NotNull String displayText,
        @NotNull String extra,
        String payload
) {

}

