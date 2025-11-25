package za.co.psybergate.application.core.utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = EncryptionUtilities.class)
public class EncryptionUtilitiesTest {

    @Autowired
    private EncryptionUtilities encryptionUtilities;

    @Test
    public void testEncrypt() {
        String secret = "mySuperSecretKey";
        String jsonBody = "{\"hello\":\"world\"}";
        String sha256Encryption = encryptionUtilities.encryptUsingSHA256(secret, jsonBody);

        Assertions.assertNotNull(sha256Encryption);
        Assertions.assertTrue(sha256Encryption.contains("sha256="));
    }

}