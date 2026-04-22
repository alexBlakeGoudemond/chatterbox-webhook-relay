package com.webhook.relay.chatterbox.application.common.logging.detail;

public interface WebhookEventLogger {

    void logReceivedWebhookEvent(String event, String delivery);

    void logCompletion(long ms);

}
