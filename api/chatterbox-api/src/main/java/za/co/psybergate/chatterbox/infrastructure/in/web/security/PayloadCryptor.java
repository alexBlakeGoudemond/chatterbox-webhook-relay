package za.co.psybergate.chatterbox.infrastructure.in.web.security;

import za.co.psybergate.chatterbox.application.exception.ApplicationException;

public interface PayloadCryptor {

    String encryptUsingSHA256(String secret, String body) throws ApplicationException;

    boolean isIdentical(String a, String b);

}
