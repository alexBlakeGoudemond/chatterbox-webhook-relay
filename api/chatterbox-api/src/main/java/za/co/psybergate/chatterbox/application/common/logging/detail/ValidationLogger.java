package za.co.psybergate.chatterbox.application.common.logging.detail;

public interface ValidationLogger {

    void logUnknownEventType(String eventType);

    void logUnrecognizedRepository(String repositoryName);

}
