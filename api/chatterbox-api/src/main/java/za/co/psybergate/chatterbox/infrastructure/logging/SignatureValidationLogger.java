package za.co.psybergate.chatterbox.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SignatureValidationLogger {

    public void logMissingSignature() {
        log.warn("[Signature] Missing signature");
    }

    public void logInvalidSignature(String expected, String received) {
        log.warn("[Signature] Invalid signature, expected={}, received={}", expected, received);
    }

    public void logValidSignature() {
        log.info("[Signature] Valid signature");
    }


}
