package za.co.psybergate.chatterbox.domain.utility;

import org.springframework.stereotype.Component;
import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class EncryptionUtilitiesImpl implements EncryptionUtilities {

    @Override
    public String encryptUsingSHA256(String secret, String body) throws InternalServerException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(body.getBytes());
            return "sha256=" + bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new InternalServerException("Failed to calculate HMAC", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /// Perform an XOR operation on each character. If at any
    /// point XOR =/= 0 - this will return FALSE
    ///
    /// **NOTE: This method does not do an early return**
    ///
    /// We want to get in the habit of writing Security code like this
    /// to avoid a [Timing Side-Channel Attack](https://en.wikipedia.org/wiki/Timing_attack),
    /// so no early returns. We want a constant-time comparison
    @Override
    public boolean isIdentical(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

}
