package za.co.psybergate.application.core.utility;

public interface EncryptionUtilities {

    String encryptUsingSHA256(String secret, String body);

    boolean isIdentical(String a, String b);

}
