package za.co.psybergate.chatterbox.domain.utility;

import za.co.psybergate.chatterbox.infrastructure.exception.ApplicationException;

public interface PayloadCryptor {

    String encryptUsingSHA256(String secret, String body) throws ApplicationException;

    boolean isIdentical(String a, String b);

}
