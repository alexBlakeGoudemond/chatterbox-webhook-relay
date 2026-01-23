package za.co.psybergate.chatterbox.application.usecase.event.processor;

public interface EventProcessor {

    void processWebhookEvents();

    void processPolledEvents();

}
