package za.co.psybergate.chatterbox.application.port.in.event.processor;

public interface EventProcessor {

    void processWebhookEvents();

    void processPolledEvents();

}
