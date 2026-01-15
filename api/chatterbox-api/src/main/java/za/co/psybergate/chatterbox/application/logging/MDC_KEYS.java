package za.co.psybergate.chatterbox.application.logging;

public enum MDC_KEYS {

    /// Used in Logging Framework as part of the MDC
    THREAD_EXECUTION_ID("threadExecutionId");

    private final String key;

    MDC_KEYS(String key) {
        this.key = key;
    }

    public String value() {
        return key;
    }

}
