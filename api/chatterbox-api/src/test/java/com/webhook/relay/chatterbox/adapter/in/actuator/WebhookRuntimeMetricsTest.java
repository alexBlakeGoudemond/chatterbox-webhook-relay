package com.webhook.relay.chatterbox.adapter.in.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookRuntimeMetricsTest {

    private MeterRegistry registry;

    private WebhookRuntimeMetrics webhookRuntimeMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        webhookRuntimeMetrics = new WebhookRuntimeMetrics(registry);
    }

    @Test
    @DisplayName("Should record signature failure with correct event type tag")
    public void whenRecordSignatureFailure_ThenMeterRegistryHasCount() {
        String eventType = "push";
        webhookRuntimeMetrics.recordSignatureFailure(eventType);
        Counter counter = registry.find("webhook.signature.failures")
                .tag("event", eventType)
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should record processing success with correct event type tag")
    public void whenRecordProcessingSuccess_ThenMeterRegistryHasCount() {
        String eventType = "pull_request";
        webhookRuntimeMetrics.recordProcessingSuccess(eventType);
        Counter counter = registry.find("webhook.payload.successes")
                .tag("event", eventType)
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

}
