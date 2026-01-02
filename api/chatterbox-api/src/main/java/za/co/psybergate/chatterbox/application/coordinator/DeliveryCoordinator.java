package za.co.psybergate.chatterbox.application.coordinator;

public interface DeliveryCoordinator {

    void processWebhookEvents();

    void processPolledEvents();

}
