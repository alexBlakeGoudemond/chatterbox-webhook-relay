package com.webhook.relay.chatterbox.application.port.in.event.processor;

public interface EventProcessorPort {

    void processWebhookEvent(String repositoryFullName);

    void processPolledEvents();

}
