package za.co.psybergate.chatterbox.application.usecase.event.processor;

public interface EventProcessorService {

    void processWebhookEvents();

    void processPolledEvents();

}
