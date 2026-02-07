package za.co.psybergate.chatterbox.application.common.logging.detail;

public interface StorageLogger {

    void logStoringEvent(Object webhook);

    void logEventStored(Object webhookEvent);

    void logDeliveringEvent(Object webhookEvent);

    void logEventDelivered(Object webhookEvent);

}
