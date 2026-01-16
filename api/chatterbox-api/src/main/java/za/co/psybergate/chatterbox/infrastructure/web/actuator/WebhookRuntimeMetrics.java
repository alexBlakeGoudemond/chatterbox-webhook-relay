package za.co.psybergate.chatterbox.infrastructure.web.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.actuator.WebhookMetrics;

/// Store some additional details in the [MeterRegistry]
/// The details captured here are stored for the lifetime of the registry.
/// The registry is destroyed when the Spring ApplicationContext is shutdown
///
/// **I.e. turning the application off deletes the Stats**
@Component
public class WebhookRuntimeMetrics implements WebhookMetrics {

    private final MeterRegistry registry;

    public WebhookRuntimeMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void recordSignatureFailure(String eventType) {
        registry.counter(
                "webhook.signature.failures",
                "event", eventType
        ).increment();
    }

    @Override
    public void recordProcessingSuccess(String eventType) {
        registry.counter(
                "webhook.payload.successes",
                "event", eventType
        ).increment();
    }

}
