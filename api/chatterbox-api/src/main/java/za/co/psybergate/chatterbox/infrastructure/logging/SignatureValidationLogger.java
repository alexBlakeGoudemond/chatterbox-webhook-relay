package za.co.psybergate.chatterbox.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// TODO BlakeGoudemond 2025/11/27 | leverage somewhere in App
@Component
@Slf4j
public class SignatureValidationLogger {

    public void logStart(String execId, String deliveryId) {
        log.info("[Signature] Starting validation execId={} delivery={}", execId, deliveryId);
    }

    public void logMismatch(String execId, String expected, String actual) {
        log.warn("[Signature] MISMATCH execId={} expected={} actual={}", execId, expected, actual);
    }

    public void logSuccess(String execId) {
        log.info("[Signature] Validation SUCCESS execId={}", execId);
    }

}
