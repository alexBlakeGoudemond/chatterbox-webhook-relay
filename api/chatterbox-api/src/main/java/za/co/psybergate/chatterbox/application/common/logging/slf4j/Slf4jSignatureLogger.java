package za.co.psybergate.chatterbox.application.common.logging.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.application.common.logging.detail.SignatureLogger;

@Slf4j
@Component
public class Slf4jSignatureLogger implements SignatureLogger {

    @Override
    public void logMissingSignature() {
        log.warn("[Signature] Missing signature");
    }

    @Override
    public void logInvalidSignature(String expected, String received) {
        log.warn("[Signature] Invalid signature, expected={}, received={}", expected, received);
    }

    @Override
    public void logValidSignature() {
        log.info("[Signature] Valid signature");
    }

}
