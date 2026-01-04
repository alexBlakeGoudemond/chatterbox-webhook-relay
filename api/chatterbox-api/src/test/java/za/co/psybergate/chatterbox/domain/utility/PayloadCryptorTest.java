package za.co.psybergate.chatterbox.domain.utility;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import za.co.psybergate.chatterbox.application.exception.ApplicationException;
import za.co.psybergate.chatterbox.application.webhook.security.PayloadCryptor;
import za.co.psybergate.chatterbox.application.webhook.security.PayloadCryptorImpl;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PayloadCryptorImpl.class)
public class PayloadCryptorTest {

    @Autowired
    private PayloadCryptor payloadCryptor;

    @Test
    @DisplayName("Encrypt with SHA256 works")
    public void whenEncryptUsingSha256_ThenSuccess() {
        String secret = "mySuperSecretKey";
        String jsonBody = "{\"hello\":\"world\"}";
        String sha256Encryption = payloadCryptor.encryptUsingSHA256(secret, jsonBody);

        assertNotNull(sha256Encryption);
        assertTrue(sha256Encryption.contains("sha256="));
    }

    @Test
    @DisplayName("Encrypt with SHA256 fails gracefully")
    public void givenNullJsonBody_whenEncryptUsingSha256_ThenFailure() {
        String secret = "mySuperSecretKey";
        String jsonBody = null;
        try {
            payloadCryptor.encryptUsingSHA256(secret, jsonBody);
        } catch (ApplicationException eexpected) {
            return;
        }

        Assertions.fail("Expected an Exception to be thrown due to the null json rawBody");
    }

    @Test
    @DisplayName("isIdentical works")
    public void whenCheckIfIdentical_ThenSuccess() {
        assertTrue(payloadCryptor.isIdentical("abc123", "abc123"));

        assertFalse(payloadCryptor.isIdentical("abc123", "def456"));
        assertFalse(payloadCryptor.isIdentical("abc123", "def456789"));
        assertFalse(payloadCryptor.isIdentical(null, "def456"));
        assertFalse(payloadCryptor.isIdentical("abc123", null));
        assertFalse(payloadCryptor.isIdentical(null, null));
    }

}