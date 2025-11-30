package za.co.psybergate.chatterbox.application.core.utility;

import za.co.psybergate.chatterbox.infrastructure.exception.ApplicationException;

public interface EncryptionUtilities {

    String encryptUsingSHA256(String secret, String body) throws ApplicationException;

    boolean isIdentical(String a, String b);

}
