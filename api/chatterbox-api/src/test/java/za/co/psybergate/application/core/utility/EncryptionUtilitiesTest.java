package za.co.psybergate.application.core.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EncryptionUtilitiesImpl.class)
public class EncryptionUtilitiesTest {

    @Autowired
    private EncryptionUtilities encryptionUtilities;

    @Test
    @DisplayName("Encrypt with SHA256 works")
    public void whenEncryptUsingSha256_ThenSuccess() {
        String secret = "mySuperSecretKey";
        String jsonBody = "{\"hello\":\"world\"}";
        String sha256Encryption = encryptionUtilities.encryptUsingSHA256(secret, jsonBody);

        assertNotNull(sha256Encryption);
        assertTrue(sha256Encryption.contains("sha256="));
    }

    @Test
    @DisplayName("isIdentical works")
    public void whenCheckIfIdentical_ThenSuccess() {
        assertTrue(encryptionUtilities.isIdentical("abc123", "abc123"));
        assertFalse(encryptionUtilities.isIdentical("abc123", "def456"));
    }

}