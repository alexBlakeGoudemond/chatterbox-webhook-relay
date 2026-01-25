package za.co.psybergate.chatterbox.infrastructure.adapter.in.web.security;

import za.co.psybergate.chatterbox.application.common.exception.ApplicationException;

public interface PayloadCryptor {

    String encryptUsingSHA256(String secret, String body) throws ApplicationException;

    boolean isIdentical(String a, String b);

}
