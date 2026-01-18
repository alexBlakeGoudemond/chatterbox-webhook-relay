package za.co.psybergate.chatterbox.application.port.in.actuator;

/// Used alongside SpringActuator to track simple usage statistics
public interface WebhookMetrics {

    /// Record Signature Failure for a specific eventType.
    /// EventTypes are bounded fields and do not change over millions of requests sent
    /// (for example `X-GitHub-Delivery`)
    void recordSignatureFailure(String eventType);

    /// Record successes for a specific eventType
    /// EventTypes are bounded fields and do not change over millions of requests sent
    /// (for example `X-GitHub-Delivery`)
    void recordProcessingSuccess(String eventType);

}
