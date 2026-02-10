package za.co.psybergate.chatterbox.application.port.in.event.processor;

public interface EventProcessorPort {

    void processWebhookEvent(String repositoryFullName);

    void processPolledEvents();

}
