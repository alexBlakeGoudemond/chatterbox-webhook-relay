package za.co.psybergate.chatterbox.application.common.event.processor;

public interface EventProcessor {

    void processWebhookEvents();

    void processPolledEvents();

}
