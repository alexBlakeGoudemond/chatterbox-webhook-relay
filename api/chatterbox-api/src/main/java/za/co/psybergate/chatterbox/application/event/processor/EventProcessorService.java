package za.co.psybergate.chatterbox.application.event.processor;

public interface EventProcessorService {

    void processWebhookEvents();

    void processPolledEvents();

}
