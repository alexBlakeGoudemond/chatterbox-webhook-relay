package com.webhook.relay.chatterbox.application.domain.configuration;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class EventPayloadMapping {

    private final String displayName;

    private final Map<IncomingMappingFieldKeys, String> fields;

    @Getter
    public enum IncomingMappingFieldKeys {
        REPOSITORYNAME("repositoryName"),
        SENDERNAME("senderName"),
        URL("url"),
        URLDISPLAYTEXT("urlDisplayText"),
        EXTRADETAIL("extraDetail");

        private final String fieldName;

        IncomingMappingFieldKeys(String fieldName) {
            this.fieldName = fieldName;
        }
    }

}
