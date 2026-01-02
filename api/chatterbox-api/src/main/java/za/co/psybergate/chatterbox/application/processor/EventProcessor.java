package za.co.psybergate.chatterbox.application.processor;

public interface EventProcessor {

    void processWebhookEvents();

    void processPolledEvents();

}
