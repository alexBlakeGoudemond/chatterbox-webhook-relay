package za.co.psybergate.chatterbox.application.webhook.security;

import za.co.psybergate.chatterbox.application.exception.ApplicationException;

public interface PayloadCryptor {

    String encryptUsingSHA256(String secret, String body) throws ApplicationException;

    boolean isIdentical(String a, String b);

}
