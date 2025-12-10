package za.co.psybergate.chatterbox.domain.utility;

import za.co.psybergate.chatterbox.infrastructure.exception.InternalServerException;

public interface PayloadCryptor {

    String encryptUsingSHA256(String secret, String body) throws InternalServerException;

    boolean isIdentical(String a, String b);

}
