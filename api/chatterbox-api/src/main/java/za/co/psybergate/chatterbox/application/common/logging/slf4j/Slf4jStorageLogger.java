package za.co.psybergate.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.detail.StorageLogger;

@Slf4j
@Component
public class Slf4jStorageLogger extends AbstractSlf4jLogger implements StorageLogger {

    @Override
    public void logStoringEvent(Object webhook) {
        log.debug("[Storage] Storing webhook event: {}", truncate(webhook));
    }

    @Override
    public void logEventStored(Object webhookEvent) {
        log.trace("[Storage] webhook event stored: {}", truncate(webhookEvent, -1));
    }

    @Override
    public void logDeliveringEvent(Object webhookEvent) {
        log.debug("[Storage] delivering webhook event: {}", truncate(webhookEvent));
    }

    @Override
    public void logEventDelivered(Object webhookEvent) {
        log.trace("[Storage] webhook event delivered: {}", truncate(webhookEvent, -1));
    }
}
