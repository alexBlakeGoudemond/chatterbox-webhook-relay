package com.webhook.relay.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.webhook.relay.chatterbox.application.common.logging.detail.ValidationLogger;

@Slf4j
@Component
public class Slf4jValidationLogger implements ValidationLogger {

    @Override
    public void logUnknownEventType(String eventType) {
        log.debug("[Validation] No ConfigurationProperties Found for eventType: {}", eventType);
    }

    @Override
    public void logUnrecognizedRepository(String repositoryName) {
        log.debug("[Validation] Repository '{}' is not whitelisted as an accepted repository", repositoryName);
    }

}
