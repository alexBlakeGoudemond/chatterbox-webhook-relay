package com.webhook.relay.chatterbox.application.common.logging.detail;

public interface StorageLogger {

    void logStoringEvent(Object webhook);

    void logEventStored(Object webhookEvent);

    void logDeliveringEvent(Object webhookEvent);

    void logEventDelivered(Object webhookEvent);

    void logPolledEventsEmpty(String repositoryFullName);

    void logWebhookEventsEmpty(String repositoryFullName);

}
