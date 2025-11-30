package za.co.psybergate.chatterbox.application.web.metric;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class WebhookMetrics {

    private final MeterRegistry registry;

    public WebhookMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /// Record Signature Failure for a specific eventType.
    /// EventTypes are bounded fields and do not change over millions of requests sent
    /// (for example `X-GitHub-Delivery`)
    public void recordSignatureFailure(String eventType) {
        registry.counter(
                "webhook.signature.failures",
                "event", eventType
        ).increment();
    }

    /// Record successes for a specific eventType
    /// EventTypes are bounded fields and do not change over millions of requests sent
    /// (for example `X-GitHub-Delivery`)
    public void recordProcessingSuccess(String eventType) {
        registry.counter(
                "webhook.signature.successes",
                "event", eventType
        ).increment();
    }

}
