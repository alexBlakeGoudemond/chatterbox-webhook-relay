package za.co.psybergate.chatterbox.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.infrastructure.filter.WebhookFilter;

@Component
@Slf4j
public class WebhookLogger {

    public void logMissingSignature() {
        log.warn("[Signature] Missing signature");
    }

    public void logInvalidSignature(String expected, String received) {
        log.warn("[Signature] Invalid signature, expected={}, received={}", expected, received);
    }

    public void logValidSignature() {
        log.info("[Signature] Valid signature");
    }

    public void logReceivedWebhookEvent(String event, String delivery) {
        log.info("[Webhook] Received event={} delivery={}", event, delivery);
    }

    public void logCompletion(long ms) {
        log.debug("[Webhook] Completed in {}ms", ms);
    }

}
