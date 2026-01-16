package za.co.psybergate.chatterbox.application.processor;

public interface EventProcessorService {

    void processWebhookEvents();

    void processPolledEvents();

}
