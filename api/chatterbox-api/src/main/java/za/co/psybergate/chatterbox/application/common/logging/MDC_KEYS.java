package za.co.psybergate.chatterbox.application.common.logging;

public enum MDC_KEYS {

    /// Used in Logging Framework as part of the MDC
    THREAD_EXECUTION_ID("threadExecutionId"),
    REPOSITORY_NAME("repositoryName");

    private final String key;

    MDC_KEYS(String key) {
        this.key = key;
    }

    public String value() {
        return key;
    }

}
